



- generic expression for a URI => scheme "://" authority "/" path [ "?" query ] [ "#" fragment ]
- "/" is used to indicate hierarchical relationship 
- api should not end with a trailing "/"
- hyphen should be used to improve readability 
- underscore must not be used as text viewer treats url as clickable
- all lowercase letters should be part of the url 
- do not use file extension ... rather use Content-Type to indicate what an url can accept
- usually api endpoint is depicted as http://api.lyftforu.com 
- resource archetype 
	+ document
		- represents a database record / object instance 
		- can have child resource to demonstrate a subordinate concept 
	+ collection 
		- server managed directory of resource 
		- client can propose to add a document into the collection, server can choose if it wants to
	+ store
		- client managed collection
		- client can PUT and DELETE from the store
	+ controller  
		- executable process available over POST verb
- singular noun for document name 
- plural noun for colleciton name 
- plural noun for store naame 
- verb phrase for controller 
- identity based value should indicate a resource 
- never use CRUD in url design, always use HTTP verb
- query ... collection/unique_id?param=1 should be used to filter / search 
- remember docroot is the top level resource 
- PUT vs. POST : PUT is where client is providing id and directly speaking with a store to keep that data it provides, POST is where the client requests server to create a resource and server decides if it needs to create one and if it creates returns back an identifier
	- diffference has been nicely discussed here : http://blue64.net/2013/10/on-deciding-between-put-and-post-when-creating-a-restful-resource/http://blue64.net/2013/10/on-deciding-between-put-and-post-when-creating-a-restful-resource/
- PUT -> should be used to additionally update existing document 
- GET and POST must not be used to tunnel other requests 
- GET is idempotent 
- POST is non - idempotent 
- PUT is idempotent 
- GET requests can be cached 
- HEAD should be used to discover state of a resource, this is exatly same as GET, only difference being HEAD does not return any API payload 
- PUT must be used to update mutable resource 
- POST must be used to create new resource 
- POST must be used to execute controller 
- DELETE must be used to remove a document from its parent 
- OPTIONS retrieves resource metadata 
- Codes
	- 1xx - informational
	- 2xx - success
		- 201 : successful creation of resources 
		- 202 : succcesful start of an asynchronous action
		- 204 : when response body is intentionally empty 
	- 3xx - redirection 
	- 4xx - client error
		- 401 : unauthorized 
		- 403 : forbidden 
		- 404 : not found 
		- 405 : method not allowed 
		- 406 : requested media type unavailable
		- 409 : violation of resource state 
		- 412 : precondition failes 
	- 5xx - server error 



