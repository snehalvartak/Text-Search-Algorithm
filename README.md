# Text-Search-Algorithm
This code was written as part of Information retrieval Coursework.

### Search Algorithm in easySearch.java
The search algorithm implemented in easySearch.java implements the below formula for ranking the documents --

![](http://www.sciweavers.org/upload/Tex2Img_1509338936/render.png)

where q is the user query, doc is the target (candidate document in AP89), t is the query term, c(t,doc) is the count of term t in document doc, N is total number of documents in AP89, and k(t) is the total number of documents that have the term t. Please use Lucene API to get the information. From retrieval viewpoint, (c(t,doc))/(length(doc)) is called normalized TF (term frequency), while log⁡(1+N/k(t) ) is IDF (inverse document frequency). 


