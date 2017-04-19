import os
import string
import time

class Cleaner:
	def clean_string(self, _string):
		'''Rid the string of common symbols, and to break it down into list of words'''
		_final_list = []
		for _word in reduce(lambda s,r: s.replace(*r), self._symbol_dictionary, _string).split():
			_word = _word.lower()

			if type(_word) == unicode:
				_word = _word.encode('ascii','ignore')

			#See if the word contains only letters or not
			if not _word.isalpha():
				continue

			#Now the word is only letters, and we can proceed to filter stopwords
			if _word not in self._stopword_dictionary[_word[0]]:
				_final_list.append(_word)
		return _final_list

	def __init__(self,_additional_symbols = [],_remove_stopwords = False, _verbose = False):
		#Used to create a dictionary of converting symbols into ''
		self._symbol_dictionary = []
		self._stopword_dictionary = {x : [] for x in string.lowercase}
		if _verbose:
			print "Cleaner:init: Initializing punctuation replacement map"
		for sym in list(string.punctuation) + [x for x in _additional_symbols if type(x) in [unicode, str]]:
			self._symbol_dictionary.append((sym,''))

		if _verbose:
			print "Cleaner:init: Initializing stopwords hashmap"
		if _remove_stopwords:
			#Write code to do so.
			stopwords_file = open(os.path.join(os.path.dirname(__file__),'stoplist_final.txt'))
			stopwords_raw = stopwords_file.read().decode('string-escape').decode("utf-8").encode('punycode').split('\n')
			for word in stopwords_raw:
				if len(word) is 0:
					continue
				word = word.lower()

				if not word.isalpha():
					continue
				# if not self.stopwords_dict.has_key(word[0]):					#Commented since we are already initializing the dictionary.
				# 	self.stopwords_dict[word[0]] = []
				self._stopword_dictionary[word[0]].append(word)
			stopwords_file.close()

