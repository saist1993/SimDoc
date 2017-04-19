import collections
#Connect to the server
def escape_chars(_text):
	# if type(_text) == unicode:
	# 	_text = _text.encode('ascii', 'ignore')
    if not type(_text) == ''.__class__:
		return _text
    # _text = _text.replace("\\","").replace("\n ","").replace("\n","")
    # _text = _text.replace('"','').replace("\\","").replace("\n ","").replace("\n","").replace("'",'')
    # _text = _text.replace('"','').replace("\\","").replace("\n ","").replace("'",'').replace("/n","")
    _text = _text.replace('"','').replace("'",'').replace("\n","").replace("/n","").replace("\\","").replace('\r', '')
    return _text
#convert unicode to string
def convert(data):
    if isinstance(data, basestring):
    	data = data.encode('ascii','ignore')
    	data = escape_chars(data)
        return data
    elif isinstance(data, collections.Mapping):
        return dict(map(convert, data.iteritems()))
    elif isinstance(data, collections.Iterable):
        return type(data)(map(convert, data))
    else:
    	if data == None:
    		data = ""
        return data