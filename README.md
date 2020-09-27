# SlimFramework
The Slim Framework is a framework I have developed for my preferred programming paradigms.
Currently, the framework is written in Kotlin targeting the Java Virtual Machine. I also have a .NET Core version far along in development I will release soon.


The first major component is a backend. The backend component is a class designed to listen for service requests in JSON format and insert them into a list, whose reference is copied into the backend at instantiation. This list is then filled with IncomingRequests (classes containing a JSON and socket) which can then be processed in a user defined switch class (who also has the list reference copied into) which does a basic parse first to determine the service, then based on the service/routing number it can be fully parsed into its respective request. This request along with the socket can then be passed into a service call.  This offloads the need to write a bunch of low-level boilerplate code just to receive service calls.

The second major componenet is a configuration manager. It enables the loading of configuration values from a file at startup and the addition of configuration values at run-time.

The third major component is a database. While a proper SQL interface is on my mind, I have designed a independent database component which allows easy CRUD  and parsing functionality. Unfortunately, this means there is not much query functionality. The only query functionality supported is key-matching record CRUD operations. Sorting will have to be done on the backend rather than the DB itself.

The fourth major component is the IPC component. This works with the backend component to enable easy service call construction and interprocess communication, especially with APIs.

The fifth major component is a logger. It's pretty basic, but it works.

The sixth major component is a wrapper for the CUPS printing system CLI. It gives a object-oriented interface to most of CUPS' features, allowing printing of files specified by name or files already loaded as byte arrays.
