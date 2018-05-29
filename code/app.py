#!/usr/bin/python
import socket
import sys
import ast
 
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

def responseFunction(buf,ans,apps_list):
    ans_arr=[]
    buf1 = buf.lower().split()
    buf=buf.split()

    if(ans == "app"):
        l1 = ast.literal_eval(apps_list)
        l2 = []
        for each in l1:
            l2.append(each.lower())
        print(l2)
        for each in buf1:
            if each in l2:
                ans_arr.append(ans)
                ans_arr.append(each)
                ans_arr.append("None")
                break
        else:
            ans_arr.append(ans)
            ans_arr.append("None")
            ans_arr.append("None")
            

        return(ans_arr)
                   

   
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
HOST = '192.168.43.204' #this is your localhost
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

con,addr = s.accept()
apps_list = con.recv(10000).decode()
print(apps_list)
print ('Socket is now listening')
while 1:
    conn, addr = s.accept()
    print ('Connect with ' + addr[0] + ':' + str(addr[1]))
    buf = conn.recv(1024).decode()
    
    print("Data received: ",buf)

    test_data = process([buf])
    pred = clf.predict(test_data)
    prob = clf.predict_proba(test_data)
    prob = prob.tolist()
    print("Probability: ",prob)
    temp1 = max(prob[0][0],prob[0][1], prob[0][2])
    print(temp1)

    if(temp1 >= 0.85):
        ans = pred[0]
        print("Prediction: ",pred)
        ans_arr=responseFunction(buf,ans,apps_list)
        functionName=ans_arr[0]
        
        objectName=ans_arr[1]
        

        bodyName=str(ans_arr[2])
        a = buf.replace(" ki "+bodyName, "")
        flag = bagofwords(a)
        if(flag==True):
            functionName = "None"
            objectName = "None"
            bodyName = "None"
        bodyName=bodyName+'\n'
        conn.send((functionName+'_'+objectName+'_'+bodyName).encode())
    else:
        functionName = "None"
        objectName = "None"
        bodyName = "None"
        conn.send((buf+"\n").encode())    
      
    
    print ("the data sent back to the client is")
    print (functionName)
    print (objectName)
    print (bodyName)
s.close()

#print pred
#print labels_test
#score = accuracy_score(labels_test,pred)
#print score




