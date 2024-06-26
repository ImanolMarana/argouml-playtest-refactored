/* $Id$
 *****************************************************************************
 * Copyright (c) 2009 Contributors - see below
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    maurelio1234
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

package org.argouml.uml.cognitive.critics;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.argouml.cognitive.Critic;
import org.argouml.cognitive.Designer;
import org.argouml.model.Model;
import org.argouml.uml.cognitive.UMLDecision;

/**
 * A critic to detect when a state has no outgoing transitions.
 *
 * @author jrobbins
 */
public class CrNoTransitions extends CrUML {

    /**
     * Constructor.
     */
    public CrNoTransitions() {
        setupHeadAndDesc();
	addSupportedDecision(UMLDecision.STATE_MACHINES);
	setKnowledgeTypes(Critic.KT_COMPLETENESS);
	addTrigger("incoming");
	addTrigger("outgoing");
    }


    /*
     * @see org.argouml.uml.cognitive.critics.CrUML#predicate2(java.lang.Object, org.argouml.cognitive.Designer)
     */
    @Override
    public boolean predicate2(Object dm, Designer dsgr) {
    if (!(Model.getFacade().isAStateVertex(dm))) {
      return NO_PROBLEM;
    }
    Object sv = /*(MStateVertex)*/ dm;
    if (Model.getFacade().isAState(sv)) {
      Object sm = Model.getFacade().getStateMachine(sv);
      if (sm != null && Model.getFacade().getTop(sm) == sv) {
        return NO_PROBLEM;
      }
    }

    if (isPseudoStateWithoutTransition(sv)) {
      return NO_PROBLEM;
    }

    boolean needsOutgoing = hasNoOutgoingTransitions(sv);
    boolean needsIncoming = hasNoIncomingTransitions(sv);

    if (Model.getFacade().isAFinalState(sv)) {
      needsOutgoing = false;
    }

    if (needsIncoming && needsOutgoing) {
      return PROBLEM_FOUND;
    }
    return NO_PROBLEM;
  }
  
  private boolean isPseudoStateWithoutTransition(Object sv) {
    if (Model.getFacade().isAPseudostate(sv)) {
      Object k = Model.getFacade().getKind(sv);
      if (k.equals(Model.getPseudostateKind().getChoice())
          || k.equals(Model.getPseudostateKind().getJunction())
          || k.equals(Model.getPseudostateKind().getInitial())) {
        return true;
      }
    }
    return false;
  }

  private boolean hasNoOutgoingTransitions(Object sv) {
    Collection outgoing = Model.getFacade().getOutgoings(sv);
    return outgoing == null || outgoing.size() == 0;
  }

  private boolean hasNoIncomingTransitions(Object sv) {
    Collection incoming = Model.getFacade().getIncomings(sv);
    return incoming == null || incoming.size() == 0;
  }
//Refactoring end
    }

    /*
     * @see org.argouml.uml.cognitive.critics.CrUML#getCriticizedMetatypes()
     */
    @Override
    public Set<Object> getCriticizedDesignMaterials() {
        Set<Object> ret = new HashSet<Object>();
        ret.add(Model.getMetaTypes().getStateVertex());
        return ret;
    }

}
