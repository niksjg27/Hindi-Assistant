#!/usr/bin/python


import numpy
from sklearn import cross_validation
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.feature_selection import SelectPercentile, f_classif

vectorizer = TfidfVectorizer(sublinear_tf = True, max_df=0.9,stop_words=['ko'],lowercase=True)
selector = SelectPercentile(f_classif, percentile=10)

def preprocess(words_file = "new_data.txt",actions_file = "new_label.txt"):
    actions_file_handler = open(actions_file, "r", encoding="utf-8")
    actions = actions_file_handler.read().split('\n')
    del(actions[-1])  
    actions_file_handler.close()

    words_file_handler = open(words_file, "r",encoding="utf-8")
    word_data = words_file_handler.read().split('\n')
    del(word_data[-1])
    words_file_handler.close()
    features_train, features_test, labels_train, labels_test = cross_validation.train_test_split(word_data, actions, test_size=0.1, random_state=40)
    
    #print (features_test)
    
    
  
    features_train_transformed = vectorizer.fit_transform(features_train)
    features_test_transformed  = vectorizer.transform(features_test)
    

    selector.fit(features_train_transformed, labels_train)
    features_train_transformed = selector.transform(features_train_transformed).toarray()
    features_test_transformed  = selector.transform(features_test_transformed).toarray()
    #print("Labels are : ", labels_train)
    return features_train_transformed, labels_train

    
def process(test):
    features_test  = vectorizer.transform(test)
    features_test  = selector.transform(features_test).toarray()
        
    print (features_test)
    return features_test


