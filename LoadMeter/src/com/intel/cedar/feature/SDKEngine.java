package com.intel.cedar.feature;

import java.io.File;
import java.util.List;
import java.util.UUID;

import javax.swing.JFrame;

import com.intel.cedar.engine.FeatureJobInfo;
import com.intel.cedar.engine.FeatureStatus;
import com.intel.cedar.engine.impl.FeatureRunner;
import com.intel.cedar.engine.impl.LocalFeatureRunner;
import com.intel.cedar.engine.model.feature.Feature;
import com.intel.cedar.engine.model.loader.FeatureDescLoader;
import com.intel.cedar.service.client.feature.model.Variable;
import com.intel.cedar.storage.IFolder;
import com.intel.cedar.storage.StorageFactory;
import com.intel.cedar.storage.impl.CedarStorage;
import com.intel.cedar.storage.impl.LocalFolder;
import com.intel.cedar.user.util.UserUtil;
import com.intel.cedar.util.EntityWrapper;
import com.intel.cedar.util.Hashes;

public class SDKEngine {
	private Feature theFeature;
	private String featureXML;
	
	public SDKEngine(String featureXML){
		this.featureXML = featureXML;
	}
	
	/**
	 * Create the GUI and show it. For thread safety, this method should be
	 * invoked from the event-dispatching thread.
	 */
	private void createAndShowGUI() {
		// Make sure we have nice window decorations.
		JFrame.setDefaultLookAndFeelDecorated(true);

		// Create and set up the window.
		JFrame frame = new JFrame("CloudTestService SDK");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Create and set up the content pane.
		SDKPanel newContentPane = new SDKPanel(theFeature.getVariables());
		newContentPane.setOpaque(true); // content panes must be opaque
		frame.setContentPane(newContentPane);

		// Display the window.
		frame.pack();
		frame.setVisible(true);
	}
	
	public void run(){
		loadFeature();
		
		/*
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
		*/
		
		System.out.println("createAndShowGUI");
		
		FeatureJobInfo jobInfo = new FeatureJobInfo();
		jobInfo.setFeatureId("theFeatureID");
		jobInfo.setDesc("blah blah");
		jobInfo.setId(Hashes.generateId(UUID.randomUUID().toString(), "Job"));
		jobInfo.setPercent(0);
		jobInfo.setStatus(FeatureStatus.Submitted);
		jobInfo.setUserId(0L);
		jobInfo.setSubmitTime(System.currentTimeMillis());
		jobInfo.setReproducable(false);
		jobInfo.setReceivers(null);
		jobInfo.setSendReport(true);
		IFolder storage = (IFolder)new CedarStorage(System.getProperty("user.dir")).getRoot();
		storage.create();
		jobInfo.setStorage(storage);
		LocalFeatureRunner runner = new LocalFeatureRunner(jobInfo, theFeature, theFeature.getVariables());
		runner.run();
	}
	
	public void loadFeature(){
		System.out.println("FeatureLoader Test begin...");
		FeatureDescLoader fload = new FeatureDescLoader();
		try{
			theFeature = fload.load(featureXML).getFeature();
		}catch(Exception e){
			System.out.println(e.getMessage());
			e.printStackTrace();			
		}		
		
		System.out.println("FeatureLoader Test end...");
	}
	
	public static void main(String[] args){
		new SDKEngine(args[0]).run();
	}
}
