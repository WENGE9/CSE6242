from util import entropy, information_gain, partition_classes
import numpy as np 
import ast

class DecisionTree(object):
    def __init__(self):
        # Initializing the tree as an empty dictionary or list, as preferred
        #self.tree = []
        self.tree = {}

    def learn(self, X, y):
        # TODO: Train the decision tree (self.tree) using the the sample X and labels y
        # You will have to make use of the functions in utils.py to train the tree
        
        # One possible way of implementing the tree:
        #    Each node in self.tree could be in the form of a dictionary:
        #       https://docs.python.org/2/library/stdtypes.html#mapping-types-dict
        #    For example, a non-leaf node with two children can have a 'left' key and  a 
        #    'right' key. You can add more keys which might help in classification
        #    (eg. split attribute and split value)
        self.tree["result"] = None
        
        maxgain = -1
                    
        if entropy(y) == 0:
            self.tree["result"]= y[0]
        elif len(X) <= 1500:
            self.tree["result"]=np.round(np.mean(y))
            
        else:
            for i in range(len(X[0])):
                
                final_split_val = np.mean(np.array(X)[:, i])
                x_left, x_right, y_left, y_right = partition_classes(X, y, i, final_split_val)
                gain = information_gain(y, [y_left, y_right])
                if gain > maxgain:
                    maxgain = gain
                    final_split_attr = i
            self.tree["split_attr"] = final_split_attr
            self.tree["split_val"] = final_split_val
            x_left, x_right, y_left, y_right = partition_classes(X, y, final_split_attr, final_split_val)
            self.tree["left"] = DecisionTree().learn(x_left, y_left)
            self.tree["right"] = DecisionTree().learn(x_right, y_right)
            
    def classify(self, record):
        # TODO: classify the record using self.tree and return the predicted label
        if self.tree["result"] is not None:
            return self.tree["result"]
        else:
            if record[self.tree["split_attr"]] <= self.tree["split_val"]:
                return self.tree["left"].classify(record)
            else:
                return self.tree["right"].classify(record)