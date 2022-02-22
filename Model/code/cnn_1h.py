from keras.models import Sequential
from keras.layers import Dense, Conv1D, Reshape, GlobalAveragePooling1D, Dropout, Input, concatenate
from keras.layers.pooling import MaxPool1D

def cnn_model_1h(input_shape, num_sensors, num_classes, time_step):
    model=Sequential()
    model.add(Reshape((time_step, num_sensors), input_shape=(input_shape,)))
    model.add(Conv1D(16, 10, activation='relu', input_shape = (time_step, num_sensors)))
    model.add(MaxPool1D(2))
    model.add(Conv1D(32, 10, activation='relu'))
    model.add(MaxPool1D(2))
    model.add(Conv1D(64, 10, activation='relu'))
    model.add(MaxPool1D(2))
    #model.add(Conv1D(128, 3, activation='relu'))
    model.add(GlobalAveragePooling1D())
    model.add(Dropout(0.5))
    model.add(Dense(num_classes, activation='softmax'))
    return model