from bottle import route, run, request
from pprint import pprint
import ujson
import sys
import time

import os
import string
import signal
import time
import pprint
import traceback
import cleaner
from gensim import models, corpora
from multiprocessing import Process, Pipe
from operator import itemgetter




# tp = topic_modeller.TopicModeller(_compiled_path, _document_stream, _cleaner)

''' The file should take model and dictionary. And then create a server and inverse hash map.'''

'''topic modeller boiler plate code '''
dname = "/home/gaurav/Downloads/dataset/20newsGroup/topic_models/topic_model_40/news20.dict" #dictionary name
mname = "/home/gaurav/Downloads/dataset/20newsGroup/topic_models/topic_model_40/lda_topic40.model" #model name

print dname

verbose = True
inverse_topic_word_map = {} #inverse topic hash map
unsaved_docs = 0
inverse_hashmap_word_lookup_length = 100

#loading dictionary and model 
print "loading dictionary and model"
dictionary = corpora.Dictionary.load(dname)
model = models.LdaModel.load(mname)

print "done loading model and dictionary"

#creating object for cleaner
cleaner = cleaner.Cleaner()

def create_inverse_hashmap(number_of_topics):
	#Generate the inverse topic hashmap
	for topicid in range(number_of_topics):
	    #Create a list of word prob tuples for a topic
	    topicid = int(topicid)
	    topic = model.state.get_lambda()[topicid] #Get words for this topic
	    topic = topic / topic.sum()
	    word_distribution_topic = [(dictionary[id], topic[id]) for id in range(len(dictionary))]
	    #Use the tuple to create the hashmap
	    for word, word_probability in word_distribution_topic:
			if not word in inverse_topic_word_map:
				inverse_topic_word_map[word] = [(topicid, word_probability)]
			else:
				inverse_topic_word_map[word].append((topicid, word_probability))


	#Sorting, trimming
	for word in inverse_topic_word_map:
		#Sort the inverse topic hashmap
		inverse_topic_word_map[word] = sorted(inverse_topic_word_map[word] ,key=itemgetter(1))[:inverse_hashmap_word_lookup_length]

def update_vocabulary(_string, _runtime = False):
		'''To update the dictionary with the string. It is cleaned and broken down into a list of words
		and then given to the dictionary to be stored. Also, every 500 documents prompts the dictionary to save itself.'''
		global unsaved_docs
		unsaved_docs = unsaved_docs + 1
		if unsaved_docs > 500:
			#if self.verbose:
			#	print "database:update_vocabulary: We've seen too many documents now. Time to save the dictionary to the disk."
			dictionary.save(dname)
		 	unsaved_docs = 0

		if _runtime:
			return dictionary.doc2bow(cleaner.clean_string(_string), allow_update = False, return_missing = False) # New words will not get (wordID, freq) if runtime is true.
		else:
			bow, newwords = dictionary.doc2bow(cleaner.clean_string(_string), allow_update = True, return_missing = True)
			new_words += len(newwords)
			# print self.new_words

		#Tell the class the number of new words encountered. 
		return bow

def find_document_topics(_string, *_sublists):
		'''This function takes a string as the input and will return both the topic distribution as well as the topic sequence as it's output.
		We might not return the topic names in human readable form but only topic IDs.
		Return type (topic_distribution, topic_sequence)'''

		start_time =  time.time()
		bow = update_vocabulary(_string, _runtime = True)  #TODO: In the case that there are some word IDs that weren't originally trained, prune them.
		
		# Subset of existing {topic} representing the document (i.e. _string). NOTHING NEW AS SUCH
		topic_distribution = model.get_document_topics(bow) 

		'''Current algorithm:
			From the global hashmap, we try to find the best suited topics for every word.
			Since the list of topic, prob is sorted already, we need to do less lookup.

			TODO: Will update algo in later commits

		'''

		
		temporary_topic_id_list = [ x[0] for x in topic_distribution ]
		temporary_unique_word_list = [ dictionary[x[0]] for x in bow ]
		temporary_inverse_topic_word_map = {}

		for word in temporary_unique_word_list:
			
			try: 
				value = inverse_topic_word_map[word]
			except KeyError:
				if verbose:
					print "Could not find the word ", word, " in the inverse hash map"
				continue

			#Fetch the sorted (topic, word_prob) list from the inverse hashmap that was created
			for topic_id, probability in value:

				#As soon as you find a match, you know that this is the best match, since the list is sorted
				if topic_id in temporary_topic_id_list:
					temporary_inverse_topic_word_map[word] = topic_id
					break

		replacetopictime = time.time()

		_topic_sequence = [temporary_inverse_topic_word_map[x] for x in _string.split() if x in temporary_inverse_topic_word_map]
		_sublists_topic_sequences = []
		for _sublist in _sublists:
			_sublists_topic_sequences.append([[temporary_inverse_topic_word_map[x] for x in _subject if x in temporary_inverse_topic_word_map] for _subject in _sublist])
		
		# print "Topic-sequencing time is ", str(time.time()-replacetopictime)
		# print "Total time is: ", str(time.time() - start_time)
		# print _string
		#Dump results
		return (topic_distribution, _topic_sequence, _sublists_topic_sequences)

#A more formal, integrated version of doing all of this.
@route('/integrated',method='POST')
def jsongateway():
    request_body = request.body.read()

    try:
        parsed_json = ujson.loads(request_body)
        print "parsed_json formed"

    except:
        print "server/hello/POST: Cannot parse JSON from the request. Is it a valid JSON request?"
        return str(-1)

    status = parsed_json['status']

    #Extracting the text and the SRO's out of JSON
    sentences_text = []
    subjects = []
    objects = []
    relations = []
    sentences = []
    start_time = time.time()    
    for x in parsed_json['data']["document_sentences"]:
       sentences_text.append(' '.join(y for y in x["processed_text"] ))
       sentences.append(x["processed_text"])
       subjects.append([x["processed_text"][int(index)] for index in x["subject"]])
       objects.append([x["processed_text"][int(index)] for index in x["object"]])
       # objects.append([x["processed_text"][int(index)] for index in x["object"]])
       relations.append([x["processed_text"][int(index)] for index in x["relation"]])
    #Use this to generate topics for the entire document.
    print sentences
    document_text = ' '.join(x for x in sentences_text)
    after_for_loop_time = time.time()   
    #To generate topics for sentences/words, we need more magic.
    print "about to generate topic for the document"
    document_topic_model, document_topic_sequence, [subject_topics, object_topics, relation_topics,sentences_topics] = find_document_topics(document_text,subjects,objects,relations,sentences)
    after_document_sequence = time.time()   
    print sentences_topics

    #Now couple the results sentence by sentence and you're covered essentially.
    parsed_json["data"]["topic_model"] = document_topic_model
    parsed_json["data"]["document_topicsequence"] = [x for x in document_topic_sequence]
    for index in range(len(parsed_json["data"]["document_sentences"])):
       parsed_json["data"]["document_sentences"][index]["subject"] = subject_topics[index]
       parsed_json["data"]["document_sentences"][index]["object"] = object_topics[index]
       parsed_json["data"]["document_sentences"][index]["relation"] = relation_topics[index]
       parsed_json["data"]["document_sentences"][index]["sentences_topics"] = sentences_topics[index]
    after_documentsequence_for_loop_time = time.time()   
    #Return it after encoding
    parsed_json = ujson.dumps(parsed_json)
    # print " @server time required after_for_loop_time ",str(after_for_loop_time - start_time),"after_documentsequence",str(after_document_sequence - after_for_loop_time),"after_documentsequence_for_loop_time",str(after_documentsequence_for_loop_time-after_document_sequence),"total_time",str(after_documentsequence_for_loop_time - start_time) 
    return parsed_json;

def main():
	number_of_topics = len(model.show_topics(num_topics = 1000))#somnehow calculate number of topics here.
	create_inverse_hashmap(number_of_topics)

if __name__ == '__main__':
	main()
	run(host="0.0.0.0", port = 9989)