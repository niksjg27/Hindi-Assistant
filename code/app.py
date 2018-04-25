#!/usr/bin/python
import socket
import sys
 
from time import time
from sklearn.naive_bayes import GaussianNB
from sklearn.svm import SVC
from sklearn.metrics import accuracy_score
from help import preprocess
from help import process


def bagofwords(text):
    negative = ["nahi","mat","na"]
    text = text.lower().split()
    print(text)
    for each in text:
        if each in negative:
            return True
    return False

def responseFunction(buf,ans):
    ans_arr=[]
    buf=buf.split()
    
    i=0
    j=0
    for i in range(len(buf)):
        temp=buf[i]
        if temp[0].isupper():
            for j in range(i,len(buf)):
                if buf[j]=="ko" or buf[j]=="KO" or buf[j]=="Ko":
                    break
            break
                
                

    body=""

    object_name=buf[i:j]
    object_name=' '.join(object_name)
    


    
    if ans=="call":
        body="None"
    if ans=="msg":
        i=0
        for i in range(len(buf)):
            if buf[i]=="ki" or buf[i]==["Ki"]:
                body=buf[i+1:]
                body=' '.join(body)
                break
        if body=="":
            body="None"
    ans_arr.append(ans)
    object_name=object_name.lower()
    ans_arr.append(object_name)
    ans_arr.append(body)
    return(ans_arr)

ans=""
HOST = '192.168.0.5' #this is your localhost
PORT = 1234
s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
print ('socket created')
try:
    s.bind((HOST, PORT))
except socket.error as err:
    print ('Bind Failed, Error Code: ' + str(err[0]) + ', Message: ' + err[1])
    sys.exit()
 
print ('Socket Bind Success!')

features_train, labels_train = preprocess()

#clf = GaussianNB()
clf = SVC(kernel='linear',probability=True)
clf.fit(features_train,labels_train)
#test = raw_input("Enter test data: ")
#test_data = process([test])
#print len(test_data)
#pred = clf.predict(test_data)

s.listen(10)
print ('Socket is now listening')
while 1:
    conn, addr = s.accept()
    print ('Connect with ' + addr[0] + ':' + str(addr[1]))
    buf = conn.recv(1024).decode()
    actual = bagofwords(buf)
    if(actual == False):
        test_data = process([buf])
        pred = clf.predict(test_data)
        prob = clf.predict_proba(test_data)
        prob = prob.tolist()
        temp1 = max(prob[0][0],prob[0][1])
        print(temp1)
        if(temp1 >= 0.85):
            if "call" in pred:
                ans="call"
            if "msg" in pred:
                ans="msg"
            ans_arr=responseFunction(buf,ans)
            functionName=ans_arr[0]
            functionName=functionName

            objectName=ans_arr[1]
            objectName=objectName

            bodyName=str(ans_arr[2])
            bodyName=bodyName+'\n'
        else:
            functionName = "None"
            objectName = "None"
            bodyName = "None\n"
    else:
        functionName = "None"
        objectName = "None"
        bodyName = "None\n"
    conn.send((functionName+'_'+objectName+'_'+bodyName).encode())
        

    
    
    print ("the data sent back to the client is")
    print (functionName)
    print (objectName)
    print (bodyName)
s.close()

#print pred
#print labels_test
#score = accuracy_score(labels_test,pred)
#print score




