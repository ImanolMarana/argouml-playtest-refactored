/* $Id$
 *****************************************************************************
 * Copyright (c) 2009 Contributors - see below
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    thn
 *****************************************************************************
 *
 * Some portions of this file was previously release using the BSD License:
 */

// Copyright (c) 1996-2006 The Regents of the University of California. All
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

package org.argouml.ui.explorer.rules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.argouml.i18n.Translator;
import org.argouml.model.Model;

/**
 * Rule for Namespace->Owned Element, 
 * excluding StateMachine, Comment and 
 * Collaborations that have a Represented Classifier or Operation.
 */
public class GoNamespaceToOwnedElements extends AbstractPerspectiveRule {

    /*
     * @see org.argouml.ui.explorer.rules.PerspectiveRule#getRuleName()
     */
    public String getRuleName() {
        return Translator.localize("misc.namespace.owned-element");
    }

    /*
     * @see org.argouml.ui.explorer.rules.PerspectiveRule#getChildren(
     *         java.lang.Object)
     */
    public Collection getChildren(Object parent) {

        if (!Model.getFacade().isANamespace(parent)) {
            return Collections.EMPTY_LIST;
        }
        Collection ownedElements = Model.getFacade().getOwnedElements(parent);
        Collection ret = new ArrayList();
        Iterator it = ownedElements.iterator();
        while (it.hasNext()) {
            Object o = it.next();
            if (shouldIncludeElement(o, parent)) {
                ret.add(o);
            }
        }
        return ret;
    }

    private boolean shouldIncludeElement(Object element, Object parent) {
        if (Model.getFacade().isACollaboration(element)) {
            if (hasRepresentedClassifierOrOperation(element)) {
                return false;
            }
        }
        if (isStateMachineOutsideContext(element, parent)) {
            return false;
        }
        if (isCommentedElement(element)) {
            return false;
        }
        if (Model.getFacade().getUmlVersion().charAt(0) == '2') {
            return handleUML2SpecificElements(element, parent);
        }
        return true;
    }

    private boolean hasRepresentedClassifierOrOperation(Object element) {
        return (Model.getFacade().getRepresentedClassifier(element) != null)
                || (Model.getFacade().getRepresentedOperation(element)
                        != null);
    }

    private boolean isStateMachineOutsideContext(Object element, Object parent) {
        return Model.getFacade().isAStateMachine(element)
                && Model.getFacade().getContext(element) != parent;
    }

    private boolean isCommentedElement(Object element) {
        return Model.getFacade().isAComment(element)
                && Model.getFacade().getAnnotatedElements(element).size() != 0;
    }

    private boolean handleUML2SpecificElements(Object element, Object parent) {
        if (Model.getFacade().isAProfileApplication(element)) {
            return false;
        }
        if (Model.getFacade().isAProfile(parent)) {
            if (Model.getFacade().isAElementImport(element)
                    || Model.getFacade().isAExtension(element)) {
                return false;
            }
        }
        if (Model.getFacade().isAStereotype(parent)
                //&& Model.getFacade().isAProperty(o)
                && Model.getFacade().getName(element) != null
                && Model.getFacade().getName(element).startsWith("base_")) {
            return false;
        }
        return true;
    }
//Refactoring end
    }

    /*
     * @see org.argouml.ui.explorer.rules.PerspectiveRule#getDependencies(
     *         java.lang.Object)
     */
    public Set getDependencies(Object parent) {
        if (Model.getFacade().isANamespace(parent)) {
	    Set set = new HashSet();
	    set.add(parent);
	    return set;
	}
	return Collections.EMPTY_SET;
    }
}
