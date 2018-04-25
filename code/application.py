#!/usr/bin/python

from time import time
from sklearn.naive_bayes import GaussianNB
from sklearn.svm import SVC
from sklearn.metrics import accuracy_score
from helper import preprocess
from image import *


features_train, features_test, labels_train, labels_test = preprocess()

#clf = GaussianNB()
clf = SVC(kernel='linear')
clf.fit(features_train,labels_train)
pred = clf.predict(features_test)
fp = open("out_pred.txt", 'w')
for i in pred:
	fp.write(i)
	fp.write('\n')
fp.close()

fp = open("out_label.txt", 'w')
for i in labels_test:
	fp.write(i)
	fp.write('\n')
fp.close()
#print pred
#print labels_test
prettyPicture(clf, features_test, pred)
output_image("test.png", "png", open("test.png", "rb").read())

score = accuracy_score(labels_test,pred)
print score




