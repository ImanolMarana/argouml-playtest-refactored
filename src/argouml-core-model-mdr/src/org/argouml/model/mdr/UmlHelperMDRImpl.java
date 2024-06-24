/* $Id$
 *****************************************************************************
 * Copyright (c) 2009 Contributors - see below
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

package org.argouml.model.mdr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.argouml.model.Model;
import org.argouml.model.UmlHelper;
import org.omg.uml.behavioralelements.collaborations.Message;
import org.omg.uml.behavioralelements.commonbehavior.Action;
import org.omg.uml.behavioralelements.commonbehavior.ActionSequence;
import org.omg.uml.behavioralelements.commonbehavior.Argument;
import org.omg.uml.behavioralelements.commonbehavior.Link;
import org.omg.uml.behavioralelements.commonbehavior.LinkEnd;
import org.omg.uml.behavioralelements.statemachines.Event;
import org.omg.uml.behavioralelements.statemachines.Transition;
import org.omg.uml.behavioralelements.usecases.Extend;
import org.omg.uml.behavioralelements.usecases.ExtensionPoint;
import org.omg.uml.behavioralelements.usecases.UseCase;
import org.omg.uml.foundation.core.AssociationEnd;
import org.omg.uml.foundation.core.Attribute;
import org.omg.uml.foundation.core.BehavioralFeature;
import org.omg.uml.foundation.core.Classifier;
import org.omg.uml.foundation.core.Enumeration;
import org.omg.uml.foundation.core.EnumerationLiteral;
import org.omg.uml.foundation.core.Feature;
import org.omg.uml.foundation.core.Operation;
import org.omg.uml.foundation.core.Parameter;
import org.omg.uml.foundation.core.Relationship;
import org.omg.uml.foundation.core.UmlAssociation;

/**
 * Helper class for UML metamodel.
 * 
 * @since ARGO0.11.2
 * @author Thierry Lach
 */
class UmlHelperMDRImpl implements UmlHelper {

    /**
     * The model implementation.
     */
    private MDRModelImplementation modelImpl;

    /**
     * Don't allow instantiation.
     * 
     * @param implementation
     *            To get other helpers and factories.
     */
    UmlHelperMDRImpl(MDRModelImplementation implementation) {
        modelImpl = implementation;
    }

    public void addListenersToModel(Object model) {
        // Nothing to do - we get all events automatically
    }

    /*
     * @see org.argouml.model.UmlHelper#deleteCollection(java.util.Collection)
     */
    public void deleteCollection(Collection col) {
        Iterator it = col.iterator();
        while (it.hasNext()) {
            modelImpl.getUmlFactory().delete(it.next());
        }
    }

    /*
     * @see org.argouml.model.UmlHelper#getSource(java.lang.Object)
     */
    public Object getSource(Object relationship) {
        if (relationship instanceof Message) {
            Message message = (Message) relationship;
            return message.getSender();
        }
        if (relationship instanceof Relationship) {
            // handles all children of relationship including extend and
            // include which are not members of core
            return modelImpl.getCoreHelper().getSource(relationship);
        }
        if (relationship instanceof Transition) {
            return modelImpl.getStateMachinesHelper().getSource(relationship);
        }
        if (relationship instanceof AssociationEnd) {
            return modelImpl.getCoreHelper().getSource(relationship);
        }
        throw new IllegalArgumentException();
    }

    /*
     * @see org.argouml.model.UmlHelper#getDestination(java.lang.Object)
     */
    public Object getDestination(Object relationship) {
        if (relationship instanceof Message) {
            Message message = (Message) relationship;
            return message.getSender();
        }
        if (relationship instanceof Relationship) {
            // handles all children of relationship including extend and
            // include which are not members of core
            return modelImpl.getCoreHelper().getDestination(relationship);
        }
        if (relationship instanceof Transition) {
            return modelImpl.getStateMachinesHelper().
                    getDestination(relationship);
        }
        if (relationship instanceof AssociationEnd) {
            return modelImpl.getCoreHelper().getDestination(relationship);
        }
        throw new IllegalArgumentException();
    }
    

    /*
     * @see org.argouml.model.UmlHelper#move(java.lang.Object, org.argouml.model.UmlHelper.Direction)
     */
    public void move(Object parent, Object element, Direction direction) {
        if (element instanceof Argument) {
            moveArgument((Argument) element, direction);
        } else if (element instanceof Action) {
            moveAction((Action) element, direction);
        } else if (element instanceof AssociationEnd) {
            moveAssociationEnd((AssociationEnd) element, direction);
        } else if (element instanceof Attribute && parent instanceof AssociationEnd) {
            moveAttributeInAssociationEnd((Attribute) element, direction);
        } else if (element instanceof Feature) {
            moveFeature((Feature) element, direction);
        } else if (element instanceof Parameter && parent instanceof Event) {
            moveParameterInEvent((Parameter) element, (Event) parent, direction);
        } else if (element instanceof Parameter) {
            moveParameter((Parameter) element, direction);
        } else if (element instanceof EnumerationLiteral) {
            moveEnumerationLiteral((EnumerationLiteral) element, direction);
        } else if (element instanceof ExtensionPoint && parent instanceof Extend) {
            moveExtensionPointInExtend((ExtensionPoint) element, (Extend) parent, direction);
        } else if (element instanceof LinkEnd) {
            moveLinkEnd((LinkEnd) element, direction);
        } else if (element instanceof ExtensionPoint && parent instanceof UseCase) {
            moveExtensionPointInUseCase((ExtensionPoint) element, (UseCase) parent, direction);
        }
    }

    private void moveArgument(Argument arg, Direction direction) {
        Action action = arg.getAction();
        List<Argument> arguments = action.getActualArgument();
        moveElementInList(arguments, arg, direction);
    }

    private void moveAction(Action action, Direction direction) {
        ActionSequence actionSequence = action.getActionSequence();
        List<Action> actions = actionSequence.getAction();
        moveElementInList(actions, action, direction);
    }

    private void moveAssociationEnd(AssociationEnd assEnd, Direction direction) {
        UmlAssociation assoc = assEnd.getAssociation();
        List<AssociationEnd> associationEnds = assoc.getConnection();
        moveElementInList(associationEnds, assEnd, direction);
    }

    private void moveAttributeInAssociationEnd(Attribute attr, Direction direction) {
        AssociationEnd assocEnd = attr.getAssociationEnd();
        List<Attribute> attributes = assocEnd.getQualifier();
        moveElementInList(attributes, attr, direction);
    }

    private void moveFeature(Feature feature, Direction direction) {
        Classifier cls = feature.getOwner();
        List features = Model.getFacade().getFeatures(cls);
        int oldIndex = features.indexOf(feature);
        int newIndex = newPosition(oldIndex, features.size(), direction);
        Model.getCoreHelper().removeFeature(cls, feature);
        Model.getCoreHelper().addFeature(cls, newIndex, feature);
    }

    private void moveParameterInEvent(Parameter param, Event event, Direction direction) {
        List<Parameter> parameters = event.getParameter();
        moveElementInList(parameters, param, direction);
    }

    private void moveParameter(Parameter param, Direction direction) {
        BehavioralFeature bf = param.getBehavioralFeature();
        List<Parameter> parameters = bf.getParameter();
        moveElementInList(parameters, param, direction);
    }

    private void moveEnumerationLiteral(EnumerationLiteral literal, Direction direction) {
        Enumeration enumeration = literal.getEnumeration();
        List<EnumerationLiteral> literals = enumeration.getLiteral();
        moveElementInList(literals, literal, direction);
    }

    private void moveExtensionPointInExtend(ExtensionPoint extensionPoint, Extend extend, Direction direction) {
        List<ExtensionPoint> extensionPoints = extend.getExtensionPoint();
        moveElementInList(extensionPoints, extensionPoint, direction);
    }

    private void moveLinkEnd(LinkEnd linkEnd, Direction direction) {
        Link link = linkEnd.getLink();
        List connections = new ArrayList(Model.getFacade().getConnections(link));
        moveElementInList(connections, linkEnd, direction);
        Model.getCoreHelper().setConnections(link, connections);
    }

    private void moveExtensionPointInUseCase(ExtensionPoint extensionPoint, UseCase useCase, Direction direction) {
        List extensionPoints = new ArrayList(Model.getFacade().getExtensionPoints(useCase));
        moveElementInList(extensionPoints, extensionPoint, direction);
        Model.getUseCasesHelper().setExtensionPoints(useCase, extensionPoints);
    }
    
    private <T> void moveElementInList(List<T> list, T element, Direction direction) {
        int oldIndex = list.indexOf(element);
        int newIndex = newPosition(oldIndex, list.size(), direction);
        list.remove(element);
        list.add(newIndex, element);
    }

    private int newPosition(int index, int size, Direction direction) {
        if (direction == Direction.DOWN) {
            return index + 1;
        } else if (direction == Direction.UP) {
            return index - 1;
        } else if (direction == Direction.TOP) {
            return 0;
        } else if (direction == Direction.BOTTOM) {
            return size - 1;
        } else {
            return 0; 
        }
    }
//Refactoring end
    
    /*
     * @see org.argouml.model.UmlHelper#move(java.lang.Object, org.argouml.model.UmlHelper.Direction)
     */
    public boolean isMovable(Object metaType) {
        final Class<?>[] movableMetaType = new Class<?> [] {
            Action.class, 
            Argument.class, 
            AssociationEnd.class, 
            Attribute.class, 
            EnumerationLiteral.class,
            Extend.class,
            ExtensionPoint.class,
            Feature.class, 
            LinkEnd.class,
            Operation.class, 
            Parameter.class};
        return Arrays.asList(movableMetaType).contains(metaType);
    }
    
    
    private int newPosition(int index, int size, Direction direction) {
        final int posn;
        if (direction == Direction.DOWN) {
            posn = index + 1;
        } else if (direction == Direction.UP) {
            posn = index - 1;
        } else if (direction == Direction.TOP) {
            posn = 0;
        } else if (direction == Direction.BOTTOM) {
            posn = size - 1;
        } else {
            posn = 0;
        }
        return posn;
    }
}
