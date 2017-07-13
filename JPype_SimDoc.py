from jpype import JPackage
from jpype import shutdownJVM
from jpype import startJVM
from jpype import getDefaultJVMPath


# startJVM(getDefaultJVMPath(), "-ea")

def connectSimDoc(document1, document2, version, simtype):
	testPkg = JPackage('com.rygbee.simdoc')
	SimDocInterface = testPkg.SimDocInterface
	# print topicvecmatrix.__class__
	result = SimDocInterface.main(document1, document2, version, simtype) 
		
	return result
	
# result1 = connectSimDoc("Hi TomENDHell go away!!", "Hi JoeENDHow are you doing?", 1)
# result2 = connectSimDoc("Hi TomENDHow are you doing?", "Hi JoeENDHow are you doing?", 2)
# print result1
# print result2
# print (0.4 * result1 + 0.6 * result2)

# print "==========================================================="

# result3 = connectSimDoc("How are you doing?ENDHi Tom", "Hi JoeENDHow are you doing?", 1)
# result4 = connectSimDoc("How are you doing?ENDHi Tom", "Hi JoeENDHow are you doing?", 2)

# print result3
# print result4
# print (0.4 * result3 + 0.6 * result4)

# shutdownJVM()	

