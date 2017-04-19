'''This script acts as a collection of classes who can perform NLP tasks, while still maintaining an object oriented tinge in the entire program flow.
We use spacy.io for most NLP tasks.
As of now, it comprises of
1. Active/Passive Voice Normalization
2. Lemmatization'''

import os, json
from spacy.en import English
from pprint import pprint
#Inhouse libs
from word_splitter import Splitter
from text_cleaner import Cleaner

class NaturalLanguageUtilities:
    '''This class serves all the basic NLP needs like extracting pos sequence, or extracting the lemmas of a text input.
    Generally heavy and thus it is adviced that not too many object of it are thrown around

    As of now, we're not giving this class the English object.
    '''
    def __init__(self):
        self.subjects = ["nsubj","nsubjpass","csubj","csubjpass",'xsubj']
        self.passive_subjects = ["nsubjpass", "csubjpass"]
        self.objects = ["agent","attr","dobj","dative","iobj","oprd",'pobj', 'acomp','prep']
        self.conjunction = ["apos","xcomp","advcl","acl",'relcl','pcomp'] #this will definitely get augmented now
        #confirm about acl
        self.prepositions = ["prep"] #not getting used right now
        self.adverbs = []
        self.adjectives = []
        self.others = []

   
                
    #flip the object and subject if you see nsubjpass, csubjpass
    #Approach: We find all the roots in the sentence first. They are basically the verbs in the sentences.
    #Now we start with the verb lowest in the dependency tree.
    #For each verb, the process is as follows: (each verb goes into relation_phrase)
    #1. Add all tokens to _temp_relations_phrase
    #2. Look at the children of the root. If one of them falls in the subj or obj list, put those subtrees under 
    #subject and object and remove them from _temp_relations_phrase. Then what remains in _temp_relations_phrase is the relations phrase.
    def SRO(self,map,sen):
        subject_phrase = [] # dictionary with key as head of the sentence which here will be root and value will be a list of words(phrase)
        object_phrase = []
        _root_token = []
        _temp_relation_phrase = []
        relation_phrase = []
        remaining_tokens = []
        for token in sen:
            remaining_tokens.append(token)
        for tokens in sen:
            # print tokens, tokens.dep_, tokens.head
            _temp_relation_phrase.append(tokens) 
            if tokens.dep_ == 'ROOT':
                _root_token.append(tokens)
                for a in tokens.subtree:
                    if a.dep_ in self.conjunction:#conjunctions also become root tokens
                        _root_token.append(a)
            ##Now you have got all the main root tokens .. relations = {"root1":"","root2":""}      
        while True:
            
            if not _root_token: #exit if there are no root tokens remaining
                break

            for root in _root_token:
                if  not len(set(list(root.subtree)).intersection(set(_root_token))) == 1:
                    continue
                is_passive_clause = False
                for tokens in root.children:
                    if tokens.dep_ in self.subjects: 
                        if tokens.dep_ in self.passive_subjects:
                            is_passive_clause = True
                        for subtokens in tokens.subtree: #add a condition here. Do this only for those dependencies which have SRO similar to their arent.
                            if subtokens in remaining_tokens:
                                try:
                                    if is_passive_clause:
                                        object_phrase.append(map[subtokens.idx])
                                    else:
                                        subject_phrase.append(map[subtokens.idx])
                                except KeyError:
                                    #This means that the word in contention was not a word and was removed by cleaner
                                    pass
                                except:
                                    print "SCREAM and run around in circles. This error wasn't supposed to be"
                                
                                _temp_relation_phrase.remove(subtokens)
                                remaining_tokens.remove(subtokens)
                    if tokens.dep_ in self.objects:
                        for subtokens in tokens.subtree:
                            if subtokens in remaining_tokens:
                                try:
                                    if is_passive_clause:
                                        subject_phrase.append(map[subtokens.idx])
                                    else:
                                        object_phrase.append(map[subtokens.idx])
                                except KeyError:
                                    #This means that the word in contention was not a word and was removed by cleaner
                                    pass
                                except:
                                    print "SCREAM and run around in circles. This error wasn't supposed to be"
                                _temp_relation_phrase.remove(subtokens)
                                remaining_tokens.remove(subtokens)
                _root_token.remove(root)
                remaining_tokens.remove(root)

        for tokens in _temp_relation_phrase:            
            try: 
                relation_phrase.append(map[tokens.idx])
            except KeyError:
                #This means that the word in contention was not a word and was removed by cleaner
                pass
            except:
                print "SCREAM and run around in circles. This error wasn't supposed to be"
        # for tokens i 
        return subject_phrase,relation_phrase,object_phrase


    def document_dictionary_builder(self,doc,cleaner):
        sentence_number = 0        
        document_package = []
        sentence_list = []
        mapper = {}
        for sen in doc.sents:
            _tempdict = {}
            _tempdict["sen_id"] = sentence_number
            _tempdict["processed_text"] = []
            tokencounter = 0
            for _tokens in sen:#for each token in each sen
                # print _tokens
                # raw_input('is _tokens one token or many?') #
                if not cleaner.isclean(_tokens.text.encode('ascii','ignore')):           #TODO: Check for a potential bug here
                
                    '''
                    !!!IMPORTANT!!!. If you find that the word here is a stopword or an alnum, simply omit this in the mapper. 
                    Now even though SRO would try to do something on it, while mapping its output, this would be omitted and the mapping remains as it's supposed to
                    '''
                    continue
                # print _tokens.idx
                # raw_input('see what idx is')
                mapper[_tokens.idx] = tokencounter
                tokencounter+=1
                _tempdict["processed_text"].append(_tokens.lemma_)
            # pprint(mapper)
            # raw_input('mapper dekho')
            # pprint(_tempdict)
            # raw_input('tempdict dekho')
            #so each token has an id. And mapper maps each token and 
            #its id to what token numbrer it is in the sentence.
            sentence = ""
            sentence = sentence + " ".join(str(x) for x in _tempdict["processed_text"])
            sentence_list.append(sentence)              

            _tempdict["subject"] = []
            _tempdict["object"] = []
            _tempdict["relation"] = []
            
            # TODO: Fix the SRO module later and then uncomment below line
            #=================================================================#
            _tempdict["subject"],_tempdict["relation"],_tempdict["object"] = self.SRO(map = mapper,sen = sen)
            
            document_package.append(_tempdict)
            sentence_number+=1

        return (document_package, sentence_list)
    
    def get_pos(self,_tokenized_doc, _as_string = False):
        '''This takes the output of spacy_object(_text) and returns the corresponding pos sequence'''
        pos_sequence = []
        for sentence in _tokenized_doc.sents:
            for word in sentence:
                pos_sequence.append(str(word.tag_))

        if _as_string:
            return ' '.join(x for x in pos_sequence)
        return pos_sequence


    def get_lemma_and_pos(self, _tokenized_doc, _as_string = False):
        '''This takes the output of spacy_object(_text) and returns the corresponding lemma sequence'''
        lemmas = []
        pos_sequence = []
        for sentence in _tokenized_doc.sents:
            for word in sentence:
                lemmas.append(str(word.lemma_))
                pos_sequence.append(str(word.tag_))
        if _as_string:
            return ' '.join(x for x in lemmas), ' '.join(x for x in pos_sequence)

        return lemmas, pos_sequence

    def remove_stopwords(self):
        #TODO Write lightweight stopword remover.
        #TODO Make lightweight stopword list
        pass


class NLPInitializer:
    '''This class overlooks and uses all the other NLP tasks and returns output to the one who calls it.'''
    def __init__(self, _short_text_length):
        self.util = NaturalLanguageUtilities()
        self.spacy_obj = English()
        self.splitter = Splitter(_verbose = False) 
        self.cleaner = Cleaner(_remove_stopwords = True, _verbose = False) # This will turn stopword remover on.
        self.cleaned_text = ""
        self.is_long_text = 0
        self._short_text_length = _short_text_length

    def flush_cleaned_text(self):
        self.cleaned_text = ""

    def process_text(self, _text):
        # print 'Text is indeed being processed.'
        #Find out if the text is in active voice. If so, then reverse the lemmatized string
        splitted_text = self.splitter.split(_text)
        # print splitted_text
        # raw_input('splitted text dekho')
        # splitted_text = _text
        # print splitted_text
        # print "RRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR"
        tokenized_doc = self.spacy_obj(splitted_text)       #This is a heavy op!
        count = 0
        token_count = 0
        
        for sentence in tokenized_doc.sents:
            if count == 0:
                self.cleaned_text = str(self.cleaned_text) + str(sentence)
                # print self.cleaned_text
                count = count + 1
            else:
                self.cleaned_text = str(self.cleaned_text) + ". " + str(sentence)
            
            token = str(sentence).split()
            token_count = token_count + len(token)

        if token_count > self._short_text_length:
            self.is_long_text = 1

        self.cleaned_text = self.cleaner.clean_string(self.cleaned_text)
        # print self.cleaned_text, " \nThis is where cleaning is done......................................................................"
       
        document_package, sentence_list = self.util.document_dictionary_builder(tokenized_doc, self.cleaner)
        # print document_package, "\n", sentence_list, "\n"
        # print "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@"
        # document_json = json.dumps(document_dictionary)
        # pprint(document_dictionary)
        return (document_package, sentence_list)


        