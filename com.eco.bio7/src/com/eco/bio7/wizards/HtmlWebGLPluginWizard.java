package com.eco.bio7.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.operation.*;
import java.lang.reflect.InvocationTargetException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import java.io.*;
import org.eclipse.ui.*;
import org.eclipse.ui.ide.IDE;

public class HtmlWebGLPluginWizard extends Wizard implements INewWizard {
	private HtmlWizardPage page;
	private ISelection selection;

	public HtmlWebGLPluginWizard() {
		super();
		setNeedsProgressMonitor(true);
		
	}

	public void addPages() {
		page = new HtmlWizardPage(selection);
		addPage(page);
	}

	public boolean performFinish() {
		final String containerName = page.getContainerName();
		final String fileName = page.getFileName();
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor)
					throws InvocationTargetException {
				try {
					doFinish(containerName, fileName, monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};
		try {
			getContainer().run(true, false, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), "Error", realException
					.getMessage());
			return false;
		}
		return true;
	}

	private void doFinish(String containerName, String fileName,
			IProgressMonitor monitor) throws CoreException {

		monitor.beginTask("Creating " + fileName, 2);
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = root.findMember(new Path(containerName));
		if (!resource.exists() || !(resource instanceof IContainer)) {
			throwCoreException("Container \"" + containerName
					+ "\" does not exist.");
		}
		IContainer container = (IContainer) resource;
		final IFile file = container.getFile(new Path(fileName));
		String an = "template";
		int index = file.getName().lastIndexOf('.');
	      if (index>0&& index <= file.getName().length() - 2 ) {
	      an=file.getName().substring(0, index);
	      }  
	   
		try {
			InputStream stream = openContentStream(an);
			if (file.exists()) {
				file.setContents(stream, true, true, monitor);
			} else {
				file.create(stream, true, monitor);
			}
			stream.close();
		} catch (IOException e) {
		}
		monitor.worked(1);
		monitor.setTaskName("Opening file for editing...");
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchPage page = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage();
				try {
					//IDE.openEditor(page, file, true);
					IDE.openEditor(page, file, true); 
				} catch (PartInitException e) {
				}
			}
		});
		monitor.worked(1);
	}

	private InputStream openContentStream(String filename) {
		String linefeed = "\r\n";
		String contents = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">"+
		"<html>"+linefeed+
		"\t<head>"+linefeed+
		   "\t\t<title>Template</title>"+linefeed+
		   "\t\t<!-- Comment -->"+linefeed+
		  "\t</head>"+linefeed+
		  "\t\t<body>"+linefeed+
		  "\t\t<canvas id=\"myCanvas\" width=\"400\" height=\"400\"></canvas>"+linefeed+
		  "\t\t<script>"+linefeed+
		 " \t\t\tvar canvas = document.getElementById('myCanvas');"+linefeed+
		 " \t\t\tvar context = canvas.getContext('webgl');"+linefeed+
		 "\t\t\t//Your code!"+linefeed+
    		"\t\t</script>"+linefeed+
		    "\t\t<p>Template</p>"+linefeed+
		 "\t</body>"+linefeed+
		"</html>";

		

		return new ByteArrayInputStream(contents.getBytes());
	}

	private void throwCoreException(String message) throws CoreException {
		IStatus status = new Status(IStatus.ERROR, "com.eco.bio7", IStatus.OK,
				message, null);
		throw new CoreException(status);
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}
}