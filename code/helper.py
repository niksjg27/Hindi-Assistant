#!/usr/bin/python


import numpy
from sklearn import cross_validation
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.feature_selection import SelectPercentile, f_classif

def preprocess(words_file = "dataset-final.txt",actions_file = "label-dataset.txt"):
    actions_file_handler = open(actions_file, "r")
    actions = actions_file_handler.read().split('\n')
    del(actions[-1])  
    actions_file_handler.close()

    words_file_handler = open(words_file, "r")
    word_data = words_file_handler.read().split('\n')
    del(word_data[-1])
    words_file_handler.close()
    
    features_train, features_test, labels_train, labels_test = cross_validation.train_test_split(word_data, actions, test_size=0.1, random_state=42)
    
    print features_test
    
    
    vectorizer = TfidfVectorizer(sublinear_tf = True, max_df=0.9,stop_words=['ko'],lowercase=True)
    features_train_transformed = vectorizer.fit_transform(features_train)
    features_test_transformed  = vectorizer.transform(features_test)
    
    selector = SelectPercentile(f_classif, percentile=10)
    selector.fit(features_train_transformed, labels_train)
    features_train_transformed = selector.transform(features_train_transformed).toarray()
    features_test_transformed  = selector.transform(features_test_transformed).toarray()

    return features_train_transformed, features_test_transformed, labels_train, labels_test

    
    


