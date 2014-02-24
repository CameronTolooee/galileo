/*
Copyright (c) 2014, Colorado State University
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

This software is provided by the copyright holders and contributors "as is" and
any express or implied warranties, including, but not limited to, the implied
warranties of merchantability and fitness for a particular purpose are
disclaimed. In no event shall the copyright holder or contributors be liable for
any direct, indirect, incidental, special, exemplary, or consequential damages
(including, but not limited to, procurement of substitute goods or services;
loss of use, data, or profits; or business interruption) however caused and on
any theory of liability, whether in contract, strict liability, or tort
(including negligence or otherwise) arising in any way out of the use of this
software, even if advised of the possibility of such damage.
 */

package galileo.client;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Map;

import galileo.comm.StorageRequest;
import galileo.dataset.Block;
import galileo.dataset.Metadata;
import galileo.net.ClientMessageRouter;
import galileo.net.NetworkDestination;
import galileo.samples.ConvertNetCDF;
import galileo.util.FileNames;
import galileo.util.Pair;
import galileo.util.ProgressBar;

public class StoreNOAA {

	private ClientMessageRouter messageRouter;
	private EventPublisher publisher;

	public StoreNOAA() throws IOException {
		messageRouter = new ClientMessageRouter();
		publisher = new EventPublisher(messageRouter);
	}

	public NetworkDestination connect(String hostname, int port)
			throws UnknownHostException, IOException {
		return messageRouter.connectTo(hostname, port);
	}

	public void disconnect() {
		messageRouter.shutdown();
	}

	public void store(NetworkDestination destination, Block block)
			throws Exception {
		StorageRequest store = new StorageRequest(block);
		publisher.publish(destination, store);
	}

	public static void main(String[] args) throws Exception {
		if (args.length != 3) {
			System.out.println("Usage: galileo.client.StoreNOAA "
					+ "<server-hostname> <server-port> <directory-name>");
			return;
		}

		String serverHostName = args[0];
		int serverPort = Integer.parseInt(args[1]);   

		StoreNOAA client = new StoreNOAA();
		File dir = new File(args[2]);
		NetworkDestination server = client.connect(serverHostName, serverPort);
		
		/* Store each grb file in the top-dir */
		for (File f : dir.listFiles()) {
			try {
			Pair<String, String> nameParts = FileNames.splitExtension(f);
			String ext = nameParts.b;
			if (ext.equals("grb") || ext.equals("bz2") || ext.equals("gz")) {
				
				/* Don't waste time parsing files that MetadataGraph is not configured for */ 
				if(!f.getName().endsWith("_000.grb.bz2")) {
						continue;
				}
				
				System.out.println("Parsing: "+ f.getName());
				Map<String, Metadata> metas = ConvertNetCDF.readFile(f.getAbsolutePath());
				System.out.println("Storing: "+ f.getName());
				int cntr = 0;
				ProgressBar pb = new ProgressBar(metas.size(), f.getName());
				for (Map.Entry<String, Metadata> entry : metas.entrySet()) {
					pb.setVal(cntr++);
					try {
					client.store(server, ConvertNetCDF.createBlock("", entry.getValue()));
					} catch (Exception e){
						System.out.println("Error uploading file: " + f.getName());
						e.printStackTrace();
						continue;
					}
				}
				pb.finish();
			}
			} catch ( Exception e){
				System.out.println("Error processing file: "+f.getName());
				e.printStackTrace();
			}
		}
		/* Hangs here so print message to denote completion TODO: fix it!*/
		System.out.println("Completed upload");
	}
}
