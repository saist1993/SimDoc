
''' 
	The program aims to evaluate SimDoc on the lee_comb dataset. 

'''
# -*- coding: utf-8 -*-
import pyorient
import os
import csv
# import script_model # This connects with STS test dataset in OrientDB
import ujson
import requests
import traceback

#interacing the code with java.

import JPype_SimDoc

from jpype import JPackage
from jpype import shutdownJVM
from jpype import startJVM
from jpype import getDefaultJVMPath
from pprint import pprint


import utils
import ujson
import time
# import model
from bottle import route, run, template, static_file, request, response,jinja2_template as template
from pprint import pprint
import traceback
import requests
import sys
import pickle

import string

import smithwaterman_v3

fname = "evaluation_dataset/LeePincombeWelsh/LeePincombeWelshDocuments.txt"
fcsv = "evaluation_dataset/LeePincombeWelsh/LeePincombeWelshData.csv"


with open(fname) as f:
	content = f.readlines()

fo = open(fcsv,'rb')
reader = csv.reader(fo)


reads = []
for row in reader:
	reads.append(row)

#similarity.dat file has topic-to-topic similarity.
f = open('similarity.dat')
d = pickle.load(f)
f.close()


fo = open("results.txt","w")
fo.close()
word2vec_matrix = []

for key in xrange(0,len(d)):
	word2vec_matrix.append(d[key])


reload(sys)
sys.setdefaultencoding('utf-8')

# print " ".join(str(x[1]) for x in line)

#Legacy code macros.
limit = 10000			#Number of test cases to consider
delimiter = "a"	#delimiter for ending sentence


# connecting to database. The database seems to be working.
# client = pyorient.OrientDB("localhost", 2424)
# db = client.db_open( "arxiv", "root", "rygbee")




# read from file and store files in an array list.

# file = open("arxiv_2014_09_27_examples.txt")
# temp_array = file.read().split("\n")
# file_array = []
# for line in temp_array:
# 	temp = line.split(" ")
# 	file_array.append(temp)

# file_rid = open("foo.txt", "wb") #to open file in append mode use a+
# max_retry = 3




nlp_url = "http://0.0.0.0:10001/hello" # server_nlp.py in NLP folder.
elasticsearch_url = "http://104.199.168.125:9200"
tp_url = "http://0.0.0.0:9989/integrated" # topic_modeller_ssimplified. 
s = requests.Session() #session objects to send requests.
startJVM(getDefaultJVMPath(), "-ea")

testPkg = JPackage('com.rygbee.simdoc')
SimDocInterface = testPkg.SimDocInterface

def constructNewsfeedDictionary(user_posts):#default show all posts
	data = []
	for post in user_posts:
		tag_list = ""
		post_dic = {}
		authors = ""
		post_dic["text"] = post["_source"]["raw_text"]
		for author in post["_source"]["authors"]:
			authors = author + " "
		post_dic['authors'] = authors
		post_dic['title'] = str(post["_source"]["title"][0])
		post_dic["scholar"] = "https://scholar.google.co.in/citations?view_op=search_authors&mauthors=%s&hl=en" % authors.replace(" ","+")
		if post["_source"]["doc_id"] == "-1":
			post_dic["type"] = "user post"
			post_tags = models.return_tags_for_post(post["_source"]["post_id"])
			post_dic['user_id'] = "-1"
			print "the tags are "
			for tags in post_tags:
				tag_list = tag_list + tags + " "
		else:
			post_dic["type"] = "Papers"
		post_dic["tags"] = tag_list
		data.append(post_dic)
	return data
def match_json(match,query):
	json = {
        "match": {
                match: {
            	"query": query
            }
        }
    }
	return json
def search_sro_generator(json_document):
	#spelling mistake
	object_sequency = []
	for objects in json_document["object_topicsequence"]:
		if objects["object_topics"]:
			object_sequency.append(match_json("object_topicsequence.object_topics",objects["object_topics"]))
	subject_sequency = []
	for subjects in json_document["subject_topicsequence"]:
		if subjects["subject_topics"]:
			subject_sequency.append(match_json("subject_topicsequence.subject_topics",subjects["subject_topics"]))
	relation_sequency = []
	for relations in json_document["relation_topicsequence"]:
		if relations["relation_topics"]:
			relation_sequency.append(match_json("relation_topicsequence.relation_topics",relations["relation_topics"]))
	return subject_sequency,relation_sequency,object_sequency	
def elasticsearch_query_generator(json_document):
	subject_topicsequence,relation_topicsequence,object_topicsequence = search_sro_generator(json_document)
	a =""
	if not subject_topicsequence:
		subject_topicsequence.append(match_json("subject_topicsequence.subject_topics",a))
	if not relation_topicsequence:
		relation_topicsequence.append(match_json("relation_topicsequence.relation_topics",a))
	if not object_topicsequence:
		object_topicsequence.append(match_json("object_topicsequence.object_topics",a))		
	query = {
	    "query": {
	        "bool": {
	            "must": [
	               {
	                   "match": {
	                      "subject_topicsequence.subject_topics": {
	                          "query": {
	                              "bool": {
	                                  "should": subject_topicsequence
	                              }
	                          }
	                      },
	                      "minimum_should_match": "70%",
	                      "boost": 3
	                   }
	               },
	               {
	                    "match": {
	                        "object_topicsequence.object_topics": {
	                            "query": {
	                              "bool": {
	                                  "should": object_topicsequence
	                              }
	                          }
	                        },
	                        "minimum_should_match": "55%",
	                        "boost": 2
	                    }
	               },
	               {
	                    "match": {
	                        "relation_topicsequence.relation_topics": {
	                            "query": {
	                              "bool": {
	                                  "should": relation_topicsequence
	                              }
	                          }
	                        }
	                    },
	                    "minimum_should_match": "5%"
	               }
	            ]
	        }
	    }
	}
	return query
def _jsonify_(doc):
	doc["data"]["nlp"] = True
	doc["data"]["tp"] = True
	subject_sequence,object_sequence,relation_sequence,sentence_sequence = updatedway_createsequence(doc["data"])
	doc["data"]["subject_topicsequence"] = subject_sequence
	doc["data"]["relation_topicsequence"] = relation_sequence
	doc["data"]["object_topicsequence"] = object_sequence
	doc["data"]["sentence_topicsequence"] = sentence_sequence
	return doc["data"]
def construct_json_object(rid,raw_text,form_question,form_answer,form_elaboration,tags,tags_array,name):
	'''constructs a json object.'''
	try:
		json_document = {}
		json_document["modelled"] = False
		json_document["vocabularized"] = False
		json_document["nlp"] = False
		json_document["tp"] = False
		json_document["doc_id"] = "-1"
		json_document["post_id"] = rid
		json_document["raw_text"] = raw_text
		json_document["form_question"] = form_question
		json_document["form_answer"] = form_answer
		json_document["form_elaboration"] = form_elaboration
		json_document["authors"] = name
		json_document["tags"] = tags
		json_document["tags"] = tags_array
		json_document["user_post"] = True
		return json_document
	except Exception, err:
		if document._rid:
			file_rid.write(document._rid)
		print traceback.format_exc()
		print "check the case"
		return -1
def filewriter(document):
	#todo - some bug here. But dont know what's the bug
	try:
		if document._rid:
			file_rid.write(document._rid)
	except:	
		pass	
	return 1

def send_request(json_request,document,url):
	try:
		response = s.post(url, data = json_request,timeout=5)
		# if url == tp_url:
		# 	print response
		# 	print response.content
		if response == -1:
			print "some error, but never mind"
			filewriter_response = filewriter(document)
			return -1
		try:
			parsedresponse = ujson.loads(response.content)
			return parsedresponse
		except Exception,err:
			print traceback.format_exc()
			filewriter_response = filewriter(document)
			return -1			
	except Exception,err:
		print traceback.format_exc()
		resend_counter = 0
		while True:
			if resend_counter == 3:
				filewriter_response = filewriter(document)
				break
			resend_counter = resend_counter + 1	
			print "record id is " , document,
			print "Error in sending request to nlp suite. Trying to send request after 30 seconds"
			time.sleep(30)
			try:
				response = s.post(url, data = json_request,timeout=5)
				print "request sent"
				return response
			except:
				print "unable to send request even after waiting 30 seconds"
				print "resending it"
		return -1
def construct_json_object_server(json_document,source):
	json_server = {}
	json_server["status"] = 32
	json_server["source"] = source
	json_server["error_code"] = 0
	json_server["data"] = {}
	json_server["data"].update(json_document)
	return json_server
def updatedway_createsequence(json_document):
	# pprint(json_document)
	subject_sequence = []
	object_sequence = []
	relation_sequence = []
	pprint(json_document)
	# print "temp objectc is "
	for sen in json_document['document_sentences']:
		subject_sequence = []
		object_sequence = []
		relation_sequence = []
		sentence_sequence = []
		# print "temp objectc is "
		for sen in json_document['document_sentences']:
			temp_subject = ""
			temp_object = ""
			temp_relation = ""
			temp_sentence = ""

			temp_subject = {}
			temp_subject["sen_id"] = sen["sen_id"]
			temp_subject["subject_topics"] = []
			for rel in sen['subject_topics']:
				try:
					temp_subject["subject_topics"].append(rel)
					# temp_subject = temp_subject + str(rel[0]) + " "
				except:
					continue
			subject_sequence.append(temp_subject)
			
			temp_subject = {}	
			temp_subject["sen_id"] = sen["sen_id"]
			temp_subject["object_topics"] = []			
			for rel in sen['object_topics']:
				try:
					temp_subject["object_topics"].append(rel)
					# temp_subject = temp_subject + str(rel[0]) + " "
				except:
					continue
			object_sequence.append(temp_subject)		
			
			temp_subject = {}	
			temp_subject["sen_id"] = sen["sen_id"]
			temp_subject["relation_topics"] = []
			for rel in sen['relation_topics']:
				try:
					# temp_subject = {}
					# temp_subject["sen_id"] = sen["sen_id"]
					temp_subject["relation_topics"].append(rel)
					# temp_subject = temp_subject + str(rel[0]) + " "
				except:
					continue
			relation_sequence.append(temp_subject)
			temp_subject = {}	
			temp_subject["sen_id"] = sen["sen_id"]
			temp_subject["sentences_topics"] = []
			for rel in sen['sentence_topics']:
				try:
					# temp_subject = {}
					# temp_subject["sen_id"] = sen["sen_id"]
					temp_subject["sentences_topics"].append(rel)
					# temp_subject = temp_subject + str(rel[0]) + " "
				except:
					continue
			sentence_sequence.append(temp_subject)
		return subject_sequence,object_sequence,relation_sequence,sentence_sequence


def post_search(document):
	'''
		>Passes the document through nlp server, for pre-processing.
		>Passes it through topic modelling.
		>return's a sequenced project.
	'''
	odb_rid = 0
	json_document = construct_json_object(rid = "",raw_text=document,form_question="",form_answer="",form_elaboration="",tags="",tags_array=[],name="")
	json_document_nlp = construct_json_object_server(json_document,"alpha")
	# pprint(json_document_nlp)
	try:
		json_document_nlp = ujson.dumps(json_document_nlp)
		# print "done with creating a json of the file to nlp server"
	except:
		print traceback.format_exc()
	# print "sending request to nlp"
	response_nlp = send_request(json_request = json_document_nlp,document = odb_rid,url = nlp_url)
	
	if response_nlp == -1:
		print "error at nlp"
	#now changin the source back to alpha
	# pprint(response_nlp)
	response_nlp["source"] = "alpha"
	response_nlp["data"]["nlp"] = True
	# print "********8done********"
	#sending this response to topicmodeller
	# print "trying to gain some information of nlp response"
	try:
		json_document_gensim = ujson.dumps(response_nlp)
	except:
		print traceback.format_exc()
	# print "trying to send to tp"
	response_tp = send_request(json_request = json_document_gensim,document = odb_rid,url = tp_url)
	if response_tp == -1:
		print "error at tp"
	json_es = _jsonify_(response_tp)
	# print "yoyoyo"
	# pprint(json_es)
	sequence = []
	document_sequence = ""
	first = True
	for sen in json_es['sentence_topicsequence']:
		if first:
			document_sequence = document_sequence + " ".join(str(x) for x in sen["sentences_topics"])
			first = False
		else:
			document_sequence = document_sequence + "".join(delimiter)
			document_sequence = document_sequence + " ".join(str(x) for x in sen["sentences_topics"])	
		sequence.append(sen['sentences_topics'])
	# print sequence
	first = True	
	return document_sequence

def return_document(link):
	'''Takes a link of the document and searches on the orientdb and 
	if found, send the paper or else return null. '''
	link = link.replace("pdf","abs")
	document = client.command('''Select from Papers where arxiv_id = "%(link)s" ''' % {"link":link} )
	try:
		document = document[0]
		return document.description
	except Exception,err:
		# print document
		# print traceback.format_exc()
		return

counter = 0
total = 0
incorrect_counter = 0
counter_simple = 0
jaccard_counter = 0
counter_bm25 = 0

def split(document):
	document = document.strip()
	document = document.split('a')
	for i in xrange(0,len(document)):
		document[i] = [ x for x in document[i].split(" ") if len(x) > 0 ]
	return document

def print_to_file(document_list,file_name):
	fo = open(file_name,"a+")
	for content in document_list:
		fo.write(str(content))
		fo.write("\n***\n")
	fo.write("\n///////////////////////////////////////////////\n\n")
	fo.close


for row in reads[1:]:
	print row
	doc1 =  content[int(row[1])-1]	
	doc2 =  content[int(row[2])-1]
	print doc1
	print doc2
	if doc1 and doc2: 
		document_A = post_search(doc1)
		document_B = post_search(doc2)
		limit = limit - 1
		sw = smithwaterman_v3.SmithWaterman(-1.4,-0.5,-0.5,0.65)
		# sw = smithwaterman_v3.SmithWaterman(-1.4,-0.5,-0.5,0.8)		#Efficiency GLOVE -> 81/100

		# sw = smithwaterman_v3.SmithWaterman(-1,-0.5,-0.5)
		result_BA_simple = sw.score(split(document_B),split(document_A))
		print result_BA_simple
	raw_input()
print "the total document correctly processed are ", counter		
shutdownJVM()