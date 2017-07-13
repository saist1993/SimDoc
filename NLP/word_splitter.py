from pprint import pprint
import pickle
import string
import time
import os


current_dir=os.path.dirname(os.path.realpath(__file__))            
#model directory is two directory down the current path
models_directory = "/"+"/".join(current_dir.split("/")[1:-2])
# print(models_directory)

class Splitter:
	'''
		The objective of this class is to fix anomalies like "hyper-active", "this,that", "istaking", "isgoing", "hyperactivity" etc.
		Basically split the words where they need to be split

		**Pseudocode**

			- automatically split words divided by a hypen or a comma without spaces between them
			- we have a list of base words (NEED TO MAKE IT MORE EXTENSIVE)
				and every word that we encounter which isn't an base word, we try to find subwords which belong to the base list. (ALGO DESCRIBED IN split_words function)
				- if we can find such subwords, cool
				- if not, we assume the word at hand to be legit and add it to the list

	'''

	def __init__(self, _verbose = True):
		'''Read the dictionary from file. Create vocab and everything'''
		self.verbose = _verbose
		self.data_directory = models_directory+'/micro_servers/NLP/data'
		self.symbol_dictionary = [ ('[', '-'), (']', '-'), ('{', '-'), ('}', '-'), ('(', '-'), (')', '-'), (',', '-')]			#Based on these symbols we split the paras

		self.new_uncommitted_words = 0
		self.new_uncommitted_words_threshold = 1000 
		
		self.cached_complex_words = {x : {} for x in string.lowercase}
		self.vocabulary = {x : [] for x in string.lowercase}

		self._train_vocabulary()

	def _train_vocabulary(self):
		if self.verbose:
			print "splitter._train_vocabulary: Starting to build vocabulary. "
		start_time = time.time()

		

		word_file = open(os.path.join('data/dictionary.txt'),'r')
		word_text = word_file.read()
		word_file.close()

		for word in word_text.split():
			self.vocabulary[word[0].lower()].append(word.lower())

		if self.verbose:
			print "splitter._train_vocabulary: Vocabulary built. Number of words: ", len(self.vocabulary), ". Time taken: ", time.time() - start_time

	def _symbol_based_breaks(self, _word):
		'''
			Splits the words appropriately based on the spaces around these symbols

			**Psedudocode**
			 - replace all commas, brakcets to hyphens
			 - split by all hyphens
			 - return all non zero string

			**Input**
				one word (no space in it)

			**Output**
				list of strings
		'''


		#Splits regardless of the presence of '-' and returns a list of words
		# words = _word.replace(',','-').split('-')
		words = reduce(lambda s,r: s.replace(*r), self.symbol_dictionary, _word).split('-')

		return [ word for word in words if len(word) > 0 ]

	def _find_sub_word_candidates(self, _word):
		'''
			Splits words into subwords. Reason for writing this is the fact that arxiv data is unclean and we need to split the words.

			Eg. randomtopic : random topic

			**Input**
				a word which surely doesn't exist in our vocabulary

			**Output**
				list of valid word tuples (can be empty too)

			**Pseudocode**
				for the length of the word,
					try to see if [0-index] slice is a valid word (eg r, ra, ran, rand, rando, random)

					if left slice is a valid word,
						see the rest of the word is a valid word too

						if yes,
							left, right are valid candidates
		'''

		#If the word already has been split once, don't even bother again
		if _word in self.cached_complex_words[_word[0].lower()]:
			return [self.cached_complex_words[_word[0].lower()][_word]]

		valid_words = []

		#Split the string in two, one word at a time. 
		for index in range(len(_word)):

			pleft =  _word[:index]

			if len(pleft) == 0:
				continue

			#If pleft is a valid word, see if the residue is valid as well
			if pleft in self.vocabulary[pleft[0].lower()]:
				pright = _word[index:]

				if len(pright) == 0:
					continue

				#If pright is a valid word too, you have a valid split word
				if pright in self.vocabulary[pright[0].lower()]:
					valid_words.append((pleft, pright))

					'''WARNING, we break here for computational time issues, but this needs to be taken into account, this might lead to some minor errors. Maybe we can later come up with a word map which effortlessly fixes all this.'''
					break

				continue
			else:
				#If Pleft is on word, skip
				continue

		return valid_words

	def _split_words(self, _word):
		'''
			Orchestrates the entire work

			**Pseudocode**
			1. Get a list of subwords split by hyphen or commas
			2. For every subword
				- if the word has non alphabets:
					directly add it to final word list
				- if not, and the word is not a valid word:
					try to find candidates of split words (randomword - random, word)
					if no candidates found, 
						assume the given word is valid and add it to vocabulary
						append it to final word list
					if candidates found
						choose the most suitable ones
						and add it to final word list
				- if the word is a valid word,
					add it to final word list

			**Input**
				a word

			**Output**
				a list of words
		'''

		word_list = self._symbol_based_breaks(_word)
		final_word_list = []

		#For every hyphen split word
		for word in word_list:

			#If the word has symbols and numbers in it, don't even bother.
			if not word.isalpha() or not len(word) >= 0:
				final_word_list.append(word)
				continue

			#Try to see if it's a valid word
			if not word in self.vocabulary[word[0].lower()]:

				#Try to split it into words
				valid_candidates = self._find_sub_word_candidates(word)

				#No valid candidates found, 
				if not len(valid_candidates) > 0:
					#Add this word into vocabulary
					# self.vocabulary.append(word)
					self.add_to_vocabulary(word)
					final_word_list.append(word)

				#If there are candidates
				else:
					#Choose one of the valid candidate
					chosen_pair = valid_candidates[0]

					#Cache this choice so that we save the processing power in the future
					self.cached_complex_words[word[0].lower()][word] = chosen_pair

					#Add these words to the final list
					final_word_list.append(chosen_pair[0])
					final_word_list.append(chosen_pair[1])

			#If it is a valid word
			else:
				final_word_list.append(word)
		return final_word_list

	def split(self, _text): 
		'''	
			Interface to the entire thing.

			**Input**
				a para of text

			**Output**
				a para of text

			**Pseudocode**
				Split the para by space
				For every word, pass to split words
		'''

		splitted_text = []

		for word in _text.split():
			splitted_text.extend(self._split_words(word))	
		#	splitted_text += self._split_words(word)

		#Join this list and send it back
		return ' '.join(x for x in splitted_text)

	def add_to_vocabulary(self, _word):
		self.vocabulary[_word[0].lower()].append(_word)
		self.new_uncommitted_words += 1

		if self.new_uncommitted_words > self.new_uncommitted_words_threshold:
			
			#TODO: Lock a file and write to it`
			pass

	def _choose_subwords(self, list):
		#Add better logic here

		#!!!DEPRICIATED!!!
		return list[0]

