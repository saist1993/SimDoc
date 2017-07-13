import math
import pickle
from pprint import pprint
#important TODO
#TODO: Create topic_matrix i.e scoring matrix
#TODO: need to write a small wrapper which gives this module input based on required interface
#TODO: convert the sentence to sentence score also in the matrix form so that it smithwaterman
#over smithwaterman could be applied

class SmithWaterman():
	"""docstring for SmithWaterman"""
	def __init__(self,topic_delete,topic_ins,topic_sub,topic_match = 0.0):
		# (-1,-0.5,-0.5,)
		# print "smithwaterman initialized"
		# self.topic_delete = -1
		# self.topic_ins =  -0.5
		# self.topic_sub = -0.5
		# self.gap_penalty = 0
		# self.topic_match = 1
		# self.topic_delete = -0.2
		# self.topic_ins =  -0.3
		# self.topic_sub = -0.15
		# self.gap_penalty = 0
		# self.topic_match = 0
		self.topic_delete = topic_delete
		self.topic_ins =  topic_ins
		self.topic_sub = topic_sub
		self.gap_penalty = 0
		self.topic_match = topic_match

		# print "Parameters: ", topic_delete, topic_ins, topic_sub
		#initializing the matrix which is a dictionary for topic topic similairty 
		self.topic_matrix = pickle.load(open('similarity_matrix.dat','rb'))

	
	def topic_similarity_lookup(self,topicid_1,topicid_2):
		try:
			# print "topicdid1:",topicid_1,"topicid2:",topicid_2,"yo"
			# return 0.0
			return self.topic_matrix[int(topicid_1)][int(topicid_2)]
		except:
			print "topicdid1:",topicid_1,"topicid2:",topicid_2,"yo"
			# print topicdid1
	def zeros(self,shape):
		retval = []
		for x in range(shape[0]):
		    retval.append([])
		    for y in range(shape[1]):
		        retval[-1].append(0)
		return retval

	def match_score(self,alpha, beta):
	    if alpha == beta:
	        return match_award
	    elif alpha == '-' or beta == '-':
	        return gap_penalty
	    else:
	        return mismatch_penalty
	
	def rmsd(self,score_list):
		squared_score = 0
		for score in score_list:
			squared_score = squared_score + score*score
		mean_square = squared_score/len(score_list)
		return math.sqrt(mean_square)
			
	def alignment_score(self,seq1,seq2,type):
	#for now type does not matter. It will be subsequently added as required.
		m, n = len(seq1), len(seq2)
		score = self.zeros((m+1, n+1))
		# print "length is ", m, n
		match = 0
		delete = 0
		insert = 0
		substitute = 0
		for i in range(1, m+1):
			for j in range(1, n+1):
				if seq1[i-1] == seq2[j-1]:
					match = score[i - 1][j - 1] + self.topic_similarity_lookup(seq1[i-1], seq2[j-1]) + self.topic_match*2.5
					score[i][j] = match
				else:
					delete = score[i - 1][j] + self.topic_delete + self.topic_similarity_lookup(seq1[i-1], seq2[j-1])*.7
					insert = score[i][j - 1] + self.topic_ins + self.topic_similarity_lookup(seq1[i-1], seq2[j-1])*.8
					substitute = score[i-1][j - 1] + self.topic_sub + self.topic_similarity_lookup(seq1[i-1], seq2[j-1])*.55
					score[i][j] = max(0,delete, insert,substitute)

				# print score[i][j], score[i][j].__class__
		# pprint(score[m-1][n-1])
		#maximum score
		# max_score = max(map(max,score)) #sw
		# pprint(score[m][n])
		# s = [[str(e) for e in row] for row in score]
		# lens = [max(map(len, col)) for col in zip(*s)]
		# fmt = '\t'.join('{{:{}}}'.format(x) for x in lens)
		# table = [fmt.format(*row) for row in s]
		# print '\n'.join(table)
		max_score = score[m][n]
		# print max_score
		if m > n:
			if m == 0: 
				return 0  	    
			return max_score/(m)
		else:
			if n == 0:
				return 0
			return max_score/(n)

	def score(self,document_1,document_2):
		scorelist = []
		for sen_doc1 in document_1:
			max_score = -500
			for sen_doc2 in document_2:
				temp_score = self.alignment_score(sen_doc1,sen_doc2,1)
				if temp_score > max_score:
					max_score = temp_score
			scorelist.append(max_score)
		# print scorelist
		# raw_input()
		rmsd_score = self.rmsd(scorelist)
		return rmsd_score

	def mean_score(self,document_1,document_2):
		scorelist = []
		for sen_doc1 in document_1:
			for sen_doc2 in document_2:
				scorelist.append(float(self.alignment_score(sen_doc1,sen_doc2,1)))
		return (sum(scorelist)/len(scorelist))		