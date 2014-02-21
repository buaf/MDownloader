package gui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;

import download.Downloader;

public class Gui {
	String listFilesPath = null;
	String outputDirName = null;

	JFrame guiFrame = null;

	JPanel inputDirPanel = null;
	JPanel outputDirPanel = null;

	JPanel overallProgressBar = null;
	JProgressBar bar = null;

	JButton startButton = null;

	boolean isDownloaded = false;
	Downloader downloader = null;

	static Gui gui;
	public Gui()
	{
		gui = this;
		guiFrame = getGuiFrame();
		guiFrame.setLayout(new GridLayout(3, 1));

		guiFrame.add(getInputDirPanel());
		guiFrame.add(getOutputDirPanel());

		guiFrame.add(getOverallProgressBar());

		guiFrame.add(getStartButtonPanel());
		guiFrame.setVisible(true);
	}

	JFrame getGuiFrame() {
		JFrame frame = new JFrame();

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("MDownloader");
		frame.setSize(350,250);
		frame.setLocationRelativeTo(null);

		return frame ;
	}

	JPanel getInputDirPanel() {
		JPanel dirFrame = new JPanel();

		JTextArea dirTextField = new JTextArea();
		dirTextField.setEditable(false);
		dirTextField.setText("Input directory or file:");
		dirFrame.add(dirTextField);

		JButton dirChoiceButton = new JButton("Choice list");
		dirChoiceButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileopen = new JFileChooser();  
				fileopen.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				int ret = fileopen.showDialog(null, "Choice list");                
				if (ret == JFileChooser.APPROVE_OPTION) {
					File file = fileopen.getSelectedFile();
					listFilesPath = file.getAbsolutePath();
				}
			}
		});
		dirFrame.add(dirChoiceButton);

		inputDirPanel = dirFrame;
		return dirFrame;
	}

	JPanel getOutputDirPanel() {
		JPanel outputDir = new JPanel();

		JTextArea outputDirTextField = new JTextArea();
		outputDirTextField.setText("Directory for download:");
		outputDirTextField.setEditable(false);
		outputDir.add(outputDirTextField);

		JButton outputDirChoiceButton = new JButton("Choice dir");
		outputDirChoiceButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileopen = new JFileChooser();  
				fileopen.setMultiSelectionEnabled(false);
				fileopen.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int ret = fileopen.showDialog(null, "Choice dir");                
				if (ret == JFileChooser.APPROVE_OPTION) {
					outputDirName = fileopen.getSelectedFile().getAbsolutePath();
					System.out.println("OUTPUT DIR:" + outputDirName);
				}
			}
		});
		outputDir.add(outputDirChoiceButton);

		outputDirPanel = outputDir;
		return outputDir;
	}

	JPanel getOverallProgressBar() {
		JPanel overallProgressBarPanel = new JPanel();

		JTextArea outputDirTextField = new JTextArea();
		outputDirTextField.setText("Overall progress:");
		outputDirTextField.setEditable(false);
		overallProgressBarPanel.add(outputDirTextField);

		JProgressBar bar = new JProgressBar();
		this.bar = bar;
		bar.setStringPainted(true);
		overallProgressBarPanel.add(bar);

		overallProgressBarPanel.setVisible(false);

		overallProgressBar = overallProgressBarPanel;
		return overallProgressBarPanel;
	}

	JPanel getStartButtonPanel() {
		JPanel startButtonPanel = new JPanel();

		final JButton startButton = new JButton("Start");
		this.startButton = startButton;
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!isDownloaded) {
					if (isPathLoaded()) {
						startButton.setText("Cancel");
						isDownloaded = true;
						inputDirPanel.setVisible(false);
						outputDirPanel.setVisible(false);
						overallProgressBar.setVisible(true);

						try {
							downloader = new Downloader(listFilesPath, outputDirName, Gui.gui);
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						downloader.startDownload();
					}
				} else {
					startButton.setText("Start");
					isDownloaded = false;
					inputDirPanel.setVisible(true);
					outputDirPanel.setVisible(true);
					overallProgressBar.setVisible(false);

					downloader = null;
					downloader.stopDownload();
				}
			}
		});
		startButtonPanel.add(startButton);

		return startButtonPanel;
	}

	boolean isPathLoaded() {
		if (listFilesPath == null) {
			JOptionPane.showMessageDialog(guiFrame, "Input file(s) not selected !");
			return false;
		}

		if (outputDirName == null) {
			JOptionPane.showMessageDialog(guiFrame, "Output directory not selected !");
			return false;
		}

		return true;
	}

	public void setBarTo(int n) {
		bar.setValue(n);
	}

	public void drawBarString(String s) {
		bar.setString(s);
	}

	synchronized public void downloadFinish() {
		if (isDownloaded) {
			JOptionPane.showMessageDialog(guiFrame, "Download finish !");
			startButton.setText("Start");
			isDownloaded = false;
			inputDirPanel.setVisible(true);
			outputDirPanel.setVisible(true);
			overallProgressBar.setVisible(false);
			downloader = null;
			downloader.stopDownload();
		}
	}
}