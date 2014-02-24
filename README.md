Galileo
=======

Galileo is a high-throughput distributed file system for multidimensional data.  It is developed at the Colorado State University Computer Science Department.  For more information about the system and its development, please see http://galileo.cs.colostate.edu/

## Setup instructions

1. Set the following environment variables: GALILEO\_HOME, GALILEO\_ROOT, and GALILEO\_CONF; where home is the path to the galileo directory, root is the path to the directory where your files are stored on the cluster, and conf is the path to the config directory (default is in the galileo directory)
     * Set these in your .bashrc/.bash_profile or .cshrc or which ever shell profile file you use (bash recommended)
2. Go to the $GALILEO_CONF/network directory and change the *.group files to select the nodes in your cluster. You can use any cs machine you'd like and have as many groups as you want
     * The format for the file is just 1 computer-name per line optionally with a colon and a port number. The default port is 5555 (which should be fine to use) you can find a list of department machines [here][machines].
     * By default there are 8 groups consisting of machines on the _lattice cluster._
3. Setup a password-less ssh login for the cs network. Instructions to do so are [here][ssh_pw]. 
5. Build the source code with _ant_ from the $GALILEO_HOME directory
6. In the galileo/bin/ directory there will be a few scripts. If everything is setup correctly, you run the galileo-cluster script and it should be all good to go. 
7. Finally, in the galileo/client package resides TextClient.java  which you can use as an example for creating and storing file blocks as well as issuing primitive queries.

Let me know if you have any questions at all.

[machines]: http://www.cs.colostate.edu/~info/machines
[ssh_pw]: http://www.cs.colostate.edu/~info/faq#4.08
