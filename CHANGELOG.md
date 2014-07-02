**1.0 : 2014/06/12 ---- First stable release**
- More updates to the net framework
- Additional samples for networking: events, synopsis, basic echo server

**0.9 : 2014/05/14 ---- Network subsystem improvements**
- Includes several new network tests
- c10k benchmarks: ~50,000 simultaneous client connections on our test hardware
- ClientMessageRouter thread safety
- event and net package improvements

**0.8 : 2014/05/08 ---- Event handling subsystem updates**
- Implementation of the reactor pattern for event handling
- Automatic annotation-based event mapping
- Usability improvements to the net subsystem
- Began migrating away from use of SelectionKeys in client code

**0.7 : 2014/04/08 ---- Geoavailability Grids, stability improvements**
- Added Geoavailability Grid implementation: spatial queries
- Refactored FileSystem implementation
- Improved Metadata journaling
- Many more samples and tests
- Fixed a critical network queueing bug

**0.6 : 2014/01/15 ---- Stable Data Model**
- galileo.dataset.feature complete beta implementation
- Networking improvements
- Initial bitmap/approximation functionality

**0.5 : 2013/05/10 ---- Stability Improvements and Partitioning Framework**
- Improved TextClient
- Added NetCDF samples

**0.4 : 2012/07/15 ---- First Public Release (alpha quality!)**
- Major architectural changes: decoupled Galileo from Granules/NaradaBrokering.
- New NIO-based network implementation
- Non-blocking client and server implementation
- File-based network setup (see ./config/network)
- Event protocol
- Data model example
- Started using Java Logging Framework


**0.3 : 2012/07/15 ---- Storage and Retrieval Enhancements**
- Hierarchical DHT prototype
- Controlled dispersion partitioning scheme
- Feature graph
- Pub: Matthew Malensek, Sangmi Pallickara, and Shrideep Pallickara.
  Expressive Query Support for Multidimensional Data in Distributed Hash Tables


**0.2 : 2012/01/30 ---- Extensions to 0.1**
- Added KML export
- Computation launch support through Granules
- Pub: Matthew Malensek, Sangmi Pallickara, and Shrideep Pallickara.
  Exploiting Geospatial and Chronological Characteristics in Data Streams to
  Enable Efficient Storage and Retrievals.


**0.1 : 2011/06/26 ---- Initial DHT implementation.**
- Initial functionality
