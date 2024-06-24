/* $Id$
 *****************************************************************************
 * Copyright (c) 2009-2012 Contributors - see below
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    bobtarling
 *****************************************************************************
 *
 * Some portions of this file was previously release using the BSD License:
 */

// Copyright (c) 1996-2007 The Regents of the University of California. All
// Rights Reserved. Permission to use, copy, modify, and distribute this
// software and its documentation without fee, and without a written
// agreement is hereby granted, provided that the above copyright notice
// and this paragraph appear in all copies.  This software program and
// documentation are copyrighted by The Regents of the University of
// California. The software program and documentation are supplied "AS
// IS", without any accompanying services from The Regents. The Regents
// does not warrant that the operation of the program will be
// uninterrupted or error-free. The end-user understands that the program
// was developed for research purposes and is advised not to rely
// exclusively on the program for any reason.  IN NO EVENT SHALL THE
// UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
// SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
// ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
// THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE. THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY
// WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
// PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
// CALIFORNIA HAS NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT,
// UPDATES, ENHANCEMENTS, OR MODIFICATIONS.

package org.argouml.uml.ui;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.Action;
import javax.swing.JFileChooser;

import org.argouml.i18n.Translator;
import org.argouml.model.Model;
import org.argouml.ui.UndoableAction;
import org.argouml.ui.targetmanager.TargetManager;
import org.argouml.uml.reveng.ImportInterface;
import org.argouml.util.ArgoFrame;


/**
 * Action to choose and set source path for model elements.
 */
public class ActionSetSourcePath extends UndoableAction {

    /**
     * The constructor.
     */
    public ActionSetSourcePath() {
        super(Translator.localize("action.set-source-path"), null);
        // Set the tooltip string:
        putValue(Action.SHORT_DESCRIPTION, 
                Translator.localize("action.set-source-path"));
    }


    /*
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
    	super.actionPerformed(e);
	File f = getNewDirectory();
	if (f != null) {
	    Object obj = TargetManager.getInstance().getTarget();
	    if (Model.getFacade().isAModelElement(obj)) {
                Object tv =
                        Model.getFacade().getTaggedValue(
                                obj, ImportInterface.SOURCE_PATH_TAG);
                if (tv == null) {
                    Model.getExtensionMechanismsHelper().addTaggedValue(
                            obj,
                            Model.getExtensionMechanismsFactory()
                                    .buildTaggedValue(
                                            ImportInterface.SOURCE_PATH_TAG,
                                            f.getPath()));
                } else {
                    Model.getExtensionMechanismsHelper().setValueOfTag(
                            tv, f.getPath());
                }
	    }
	}
    }

    /**
     * @return the new source path directory
     */
    protected File getNewDirectory() {
        Object obj = TargetManager.getInstance().getTarget();
        if (!Model.getFacade().isAModelElement(obj)) {
            return null;
        }

        String name = Model.getFacade().getName(obj);
        String type = getType(obj);
        String path = getPath(obj);

        JFileChooser chooser = createFileChooser(path);
        chooser.setSelectedFile(path != null ? new File(path) : null);

        String chooserTitle =
            Translator.localize("action.set-source-path") + ' ' + type
                + (name != null ? ' ' + name : "");
        chooser.setDialogTitle(chooserTitle);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int retval =
            chooser.showDialog(ArgoFrame.getFrame(),
                Translator.localize("dialog.button.ok"));
        return retval == JFileChooser.APPROVE_OPTION ? chooser.getSelectedFile() : null;
    }
    
    private String getPath(Object obj) {
        Object tv = Model.getFacade().getTaggedValue(obj,
                ImportInterface.SOURCE_PATH_TAG);
        return tv != null ? Model.getFacade().getValueOfTag(tv) : null;
    }

    private String getType(Object obj) {
        if (Model.getFacade().isAPackage(obj)) {
            return "Package";
        } else if (Model.getFacade().isAClass(obj)) {
            return "Class";
        } else if (Model.getFacade().isAInterface(obj)) {
            return "Interface";
        }
        return null;
    }

    private JFileChooser createFileChooser(String path) {
        if (path != null) {
            File f = new File(path);
            if (f.getPath().length() > 0) {
                return new JFileChooser(f.getPath());
            }
        }
        return new JFileChooser();
    }

//Refactoring end

	String sChooserTitle =
	    Translator.localize("action.set-source-path");
	if (type != null) {
            sChooserTitle += ' ' + type;
        }
	if (name != null) {
            sChooserTitle += ' ' + name;
        }
	chooser.setDialogTitle(sChooserTitle);
	chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

	int retval =
            chooser.showDialog(ArgoFrame.getFrame(),
                    Translator.localize("dialog.button.ok"));
	if (retval == JFileChooser.APPROVE_OPTION) {
	    return chooser.getSelectedFile();
	} else {
	    return null;
	}
    }

    /**
     * The UID.
     */
    private static final long serialVersionUID = -6455209886706784094L;
}
