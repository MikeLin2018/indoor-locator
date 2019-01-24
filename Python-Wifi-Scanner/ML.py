import pandas as pd
import numpy as np
from sklearn import linear_model
from sklearn import metrics
from sklearn.model_selection import train_test_split
import pickle
from scanner import CSV_Manager

# Get the data
file_name = 'Room1Data5 remove-zero add-room-num and Room2Data1 remove-zero add-room-num merged and Room3Data1 remove-zero add-room-num merged remove-header.csv'
wifi_data_headers = CSV_Manager.get_header('Room1Data5 remove-zero add-room-num and Room2Data1 remove-zero add-room-num merged and Room3Data1 remove-zero add-room-num merged.csv')
wifi_data = pd.read_csv(file_name, names = wifi_data_headers)
train_x, test_x, train_y, test_y = train_test_split(wifi_data[wifi_data_headers[1:]], wifi_data[wifi_data_headers[0]], train_size=0.7)

# Train multi-classification model with logistic regression
lr = linear_model.LogisticRegression()
lr.fit(train_x, train_y)

# Train multinomial logistic regression model
mul_lr = linear_model.LogisticRegression(multi_class='multinomial', solver='newton-cg').fit(train_x, train_y)

# Save the model
save_to = "Trained mul_lr for Room1,2,3"
pickle.dump(mul_lr, open(save_to, 'wb'))

# Print the accuracy
print("Logistic regression Train Accuracy :: ", metrics.accuracy_score(train_y, lr.predict(train_x)))
print("Logistic regression Test Accuracy :: ", metrics.accuracy_score(test_y, lr.predict(test_x)))
print("Multinomial Logistic regression Train Accuracy :: ", metrics.accuracy_score(train_y, mul_lr.predict(train_x)))
print("Multinomial Logistic regression Test Accuracy :: ", metrics.accuracy_score(test_y, mul_lr.predict(test_x)))
