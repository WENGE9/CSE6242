import http.client
import json
import time
import timeit
import sys
import collections
from pygexf.gexf import *


#
# implement your data retrieval code here
#

api=str(sys.argv[1])
#print(api)
web="www.rebrickable.com"
h = http.client.HTTPConnection(web)
#========================================================================================
h.request("GET","https://rebrickable.com/api/v3/lego/sets/?key="+api+'&page_size=270&&min_parts=1000&ordering=-num_parts%2Cid')
r = h.getresponse()

print(r)
print(r.status, r.reason)

json_sets = json.load(r)
#print(type(json_sets))
#print(json_sets['results'][-1])
#print(json_sets)
#complete auto grader functions for Q1.1.b,d

def min_parts():
    """
    Returns an integer value
    """
    # you must replace this with your own value
    # Load JSON:
    #json_sets = json.load(r)
    return json_sets['results'][-1]['num_parts']

print(min_parts())
#print(type(json_sets['results'][-1]))
def lego_sets():
    """
    return a list of lego sets.
    this may be a list of any type of values
    but each value should represent one set
    
    e.g.,
    biggest_lego_sets = lego_sets()
    print(len(biggest_lego_sets))
    > 280
    e.g., len(my_sets)
    """
    
    # you must replace this line and return your own list
    return json_sets['results']
#print(type(json_sets['results']))
print(len(lego_sets()))
#==============================================================================================
a_sets = json_sets['results']
#print(type(a_sets))
parts_StoredForGraph={"set_num": list}

for set in a_sets:
    set_num=str(set['set_num'])
    parts_StoredForGraph[set_num]=list()
    h.request("GET","https://rebrickable.com/api/v3/lego/sets/"+set_num+"/parts/?key="+api+'&page_size=50&is_spare=True&min_quantity=1400&ordering=-results_quantity%2Cid')
    r1 = h.getresponse()
    #print(r1.status,r1.reason)
    json_data_part = json.load(r1)
    parts_fromWeb=json_data_part['results']
    i=0
    for p in parts_fromWeb:
            
            i=i+1
            partNew={'num': '','color': '','quantity': '','ID': '','name': '',}
            #print(part['part'])
            num=p['part']['part_num']
            partNew['part_num']=num
            color=p['color']['rgb']
            partNew['color']=color
            partNew['name']=p['part']['name']
            partNew['quantity']=p['quantity']
            partNew['ID']=num+'_'+color
            parts_StoredForGraph[set_num].append(partNew)
            if(i>20):
                break

#print(parts_StoredForGraph)
def gexf_graph():
    """
    return the completed Gexf graph object
    """
    # you must replace these lines and supply your own graph
    my_gexf = Gexf("Hang", "bricks_graph")
    graph=my_gexf.addGraph("undirected", "static", "bricks_graph")


    att123=graph.addNodeAttribute(title='type',type='string')
    for set in a_sets:
        num=set['set_num']
        graph.addNode(num,set['name'],r='0',g='0',b='0').addAttribute(att123,"set")

        for p in parts_StoredForGraph[num]:
            graph.addNode(p['ID'],p['name'],
                          
                          r=str(int(str(p['color'][0:2]),16)),
                          g=str(int(str(p['color'][2:4]),16)),
                          b=str(int(str(p['color'][4:6]),16))
                          ).addAttribute(att123,"part")

            graph.addEdge(set['set_num']+p['ID']+str(p['quantity']),set['set_num'],p['ID'],weight=p['quantity'])
            
    output_file=open('bricks_graph.gexf','wb')
    my_gexf.write(output_file)
        
    return my_gexf.graphs[0]

gexf_graph();
# complete auto-grader functions for Q1.2.d

def avg_node_degree():
    """
    hardcode and return the average node degree
    (run the function called “Average Degree”) within Gephi
    """
    # you must replace this value with the avg node degree
    return 8.43

def graph_diameter():
    """
    hardcode and return the diameter of the graph
    (run the function called “Network Diameter”) within Gephi
    """
    # you must replace this value with the graph diameter
    return 8

def avg_path_length():
    """
    hardcode and return the average path length
    (run the function called “Avg. Path Length”) within Gephi
    :return:
    """
    # you must replace this value with the avg path length
    return 3.982
