
                            # This is a stand-alone server file#
# =========================================================================================#

from bottle import route, run, request
from pprint import pprint
import json
import ujson
import os
import sys
import traceback
import ConfigParser #for reading path of models folder from some .ini file

short_text_length = 0

current_dir=os.path.dirname(os.path.realpath(__file__))            
#model directory is two directory down the current path
models_directory = "/"+"/".join(current_dir.split("/")[1:-2])
print(models_directory)

def add_to_sys_path(path_following_models):
    # config = ConfigParser.ConfigParser()        
    CONFIG_FILE = 'development_imports.ini'
    # config.read(CONFIG_FILE)
    sys.path.insert(0,models_directory+path_following_models)
    
    '''
    if path_following_models is "system_setup":
        # models_directory = config.get('FILE','models_file_path')
        sys.path.insert(0,models_directory)

        config_dev = ConfigParser.ConfigParser()
        system_config_info_directory = models_directory+"/development_test.ini" #config.get('FILE','system_config_info')
        sys.path.insert(0,system_config_info_directory)
        DEFAULT_CONFIG = "development_test.ini"

        if len(sys.argv) > 1:
            try:
                #See if the file exists
                filename = sys.argv[1]
                if filename[-4:] == '.ini' and filename in os.listdir('.'):
                    #Now read it then
                    config_dev.read(sys.argv[1])
                else:
                    print "main: Cannot read the config file. Proceeding with the default (development) settings. Press Ctrl+C to stop"
                    config_dev.read(DEFAULT_CONFIG)
            except:
                print traceback.format_exc()
                print "main: Cannot read the config file. Proceeding with the default (development) settings. Press Ctrl+C to stop"
                config_dev.read(DEFAULT_CONFIG)
        else:
            config_dev.read(DEFAULT_CONFIG)

        short_text_length = safely_get_value('NLP', 'short text length', _datatype = 'int', config_dev = config_dev)
        print short_text_length
        print "================="
    else:
        # models_directory = config.get('FILE','models_file_path')
        sys.path.insert(0,models_directory+path_following_models)
    '''    
def safely_get_value(_section, _option, _datatype = None, _allow_none = False, config_dev = None):
    try:
        if _datatype == 'boolean':
            var = config_dev.getboolean(_section, _option)
        elif _datatype == 'int':
            var = config_dev.getint(_section, _option)
        else:
            var = config_dev.get(_section, _option)
    except:
        print traceback.format_exc()
        print "main: ERROR: Can't find the %(section)s's %(option)s. Please enter it manually." % {'section': _section, 'option': _option}
        var = raw_input("[HINT] Leave it blank to exit the program")

        if var == '':
            pass #EXIT

        if _datatype == 'boolean':
            if var in ['True', 'False']:
                var = True if var == 'True' else False
            else:
                pass #EXIT

        if _datatype == 'int':
            if var.isdigit():
                var = int(var)
            else:
                pass #EXIT

    if var is 'None':
        var = None
    return var

add_to_sys_path('/micro_servers/NLP')
# add_to_sys_path('system_setup') #adding the path inside models folder to sys.path

# from ..NLP.SRO_analyzer import NLPInitializer
from SRO_analyzer import NLPInitializer

@route('/test', method='GET')
def test():
    return "NLP server is accessible and is working properly"


@route('/integrated', method='POST')
def hello():
    #Read the request from the HTTP Body
    request_data =  request.body.read()
    #  print "the request data is "
    #print request_data
    #Try and see if we can parse it as JSON. If not, return -1
    try:
        parsedresponse = ujson.loads(request_data)
    except ValueError:
        print "server/hello/POST: Cannot parse JSON from the request. Is it a valid JSON request?"
        return str(-1)

    #The request is then JSON. Let's open it up
    raw_text = parsedresponse['data']['raw_text']
    # print "raw_text is - sendin it to spacy"
    # print raw_text
    # pprint(parsedresponse)
    
    # processed_text = nlp.process_text(raw_text)
    # parsedresponse['data']['document_sentences'] = []
    parsedresponse['data']['document_sentences'], parsedresponse['data']['sentence_list'] = nlp.process_text(raw_text)
    
    # print parsedresponse['data']['sentence_list']
    # print "RRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR"
    error_code = 0
    #Constructing the response
    parsedresponse['source'] = "natural_language_processing"
    parsedresponse['long_text'] = nlp.is_long_text

    parsedresponse['data']['cleaned_text'] = nlp.cleaned_text
    parsedresponse = ujson.dumps(parsedresponse)
    nlp.flush_cleaned_text()

    # pprint("parsedresponse")
    # print "RRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR"

    #Send response
    # print "this is the spacy reposnse"
    # print response
    return parsedresponse

#======================================================================================================#
                        # For some reason nlp_integrated is getting called from view

#======================================================================================================#
@route('/hello',method='POST')
def nlp_integrated():
    #Read the request from the HTTP Body
    request_data =  request.body.read()
    #  print "the request data is "
    #print request_data
    #Try and see if we can parse it as JSON. If not, return -1
    try:
        parsedresponse = ujson.loads(request_data)
    except ValueError:
        print "server/hello/POST: Cannot parse JSON from the request. Is it a valid JSON request?"
        return str(-1)

    #The request is then JSON. Let's open it up
    raw_text = parsedresponse['data']['raw_text']
    # print "raw_text is - sendin it to spacy"
    print raw_text
    print "RRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR"
    # pprint(parsedresponse)
    
    # processed_text = nlp.process_text(raw_text)
    # parsedresponse['data']['document_sentences'] = []
    parsedresponse['data']['document_sentences'], parsedresponse['data']['sentence_list'] = nlp.process_text(raw_text)
    
    # print parsedresponse['data']['sentence_list']
    # print "RRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR"
    error_code = 0
    #Constructing the response
    parsedresponse['source'] = "natural_language_processing"
    parsedresponse['long_text'] = nlp.is_long_text

    parsedresponse['data']['cleaned_text'] = nlp.cleaned_text
    parsedresponse = ujson.dumps(parsedresponse)
    nlp.flush_cleaned_text()

    # pprint("parsedresponse")
    # print "RRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR"

    #Send response
    # print "this is the spacy reposnse"
    # print response
    return parsedresponse



@route('/hellos',method='POST')
def nlp_integrated():
    #Read the request from the HTTP Body
    request_data =  request.body.read()
    # print "the request data is "
    # print len(ujson.loads(request_data))
    request_data = ujson.loads(request_data)
    # return 1
    #Try and see if we can parse it as JSON. If not, return -1
    response_data=[]
    read_counter=1
    for parsedresponse in request_data: 
        try:
            # parsedresponse = ujson.loads(request_data)
            print("reading doc:"+ str(read_counter))
            read_counter = read_counter + 1
        except ValueError:
            print "server/hello/POST: Cannot parse JSON from the request. Is it a valid JSON request?"
            return str(-1)

        #The request is then JSON. Let's open it up
        raw_text = parsedresponse['data']['raw_text']
        # print "raw_text is - sendin it to spacy"
        # print raw_text
        # print "RRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR"
        # pprint(parsedresponse)
        
        # processed_text = nlp.process_text(raw_text)
        # parsedresponse['data']['document_sentences'] = []
        parsedresponse['data']['document_sentences'], parsedresponse['data']['sentence_list'] = nlp.process_text(raw_text)
        
        # print parsedresponse['data']['sentence_list']
        # print "RRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR"
        error_code = 0
        #Constructing the response
        parsedresponse['source'] = "natural_language_processing"
        parsedresponse['long_text'] = nlp.is_long_text

        parsedresponse['data']['cleaned_text'] = nlp.cleaned_text
        # parsedresponse = ujson.dumps(parsedresponse)
        nlp.flush_cleaned_text()
        response_data.append(parsedresponse)
        # pprint("parsedresponse")
        # print "RRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR"

        #Send response
        # print "this is the spacy reposnse"
        # print response
    response_data=ujson.dumps(response_data)
    # print(type(response_data))
    # print(len(response_data))
    return response_data

#======================================================================================================
if __name__ == "__main__":
    nlp = NLPInitializer(_short_text_length = 5000)

        #Check the server address (the micro-server is stand-alone and has to be triggered manually)
    #======================================================================================================#
    # run(host='localhost', port=10001, debug=True,server='gunicorn', workers=4)
    run(host='0.0.0.0', port=10001, debug=False,server = 'eventlet',workers = 1)
    # ip = 'http://127.0.0.1:10001/hello'
    # run(host='127.0.0.1', port=10001, debug=False,server = 'gunicorn',workers = 1)