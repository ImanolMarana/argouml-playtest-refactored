/* $Id$
 *******************************************************************************
 * Copyright (c) 2009-2012 Contributors - see below
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Bob Tarling - Original implementation
 *******************************************************************************
 */

package org.argouml.core.propertypanels.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.DefaultListModel;
import javax.swing.SwingUtilities;

import org.argouml.core.propertypanels.model.GetterSetterManager;
import org.argouml.kernel.Command;
import org.argouml.model.AddAssociationEvent;
import org.argouml.model.AttributeChangeEvent;
import org.argouml.model.InvalidElementException;
import org.argouml.model.Model;
import org.argouml.model.RemoveAssociationEvent;
import org.argouml.util.CollectionUtil;

/**
 * The simplest model for a list of UML elements.
 */
class SimpleListModel
        extends DefaultListModel implements PropertyChangeListener {

    /**
     * The class uid
     */
    private static final long serialVersionUID = -8491023641828449064L;

    private static final Logger LOG =
        Logger.getLogger(SimpleListModel.class.getName());

    /**
     * The metatypes to provide buttons to create.
     */
    private final List<Class<?>> metaTypes;

    private final Object umlElement;
    private final String propertyName;

    final private GetterSetterManager getterSetterManager;

    SimpleListModel(
            final String propertyName,
            final List<Class<?>> types,
            final Object umlElement,
            final GetterSetterManager getterSetterManager) {
        super();
        this.getterSetterManager = getterSetterManager;
        metaTypes = types;
        this.propertyName = propertyName;
        this.umlElement = umlElement;

        build();

        Model.getPump().addModelEventListener(this, umlElement, propertyName);
    }

    public void removeModelEventListener() {
        Model.getPump().removeModelEventListener(this, umlElement, propertyName);
    }

    public Object getMetaType() {
        if (metaTypes.size() > 0) {
            return metaTypes.get(0);
        }
        return getterSetterManager.getMetaType(propertyName);
    }

    public String getPropertyName() {
    	return propertyName;
    }

    public List getMetaTypes() {
        return metaTypes;
    }

    public Command getRemoveCommand(Object objectToRemove) {
    	return getterSetterManager.getRemoveCommand(propertyName, umlElement, objectToRemove);
    }

    public Command getAddCommand() {
    	return getterSetterManager.getAddCommand(propertyName, umlElement);
    }

    public List<Command> getAdditionalCommands() {
    	return getterSetterManager.getAdditionalCommands(propertyName, umlElement);
    }

    /*
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(final PropertyChangeEvent e) {
        if (e instanceof RemoveAssociationEvent || e instanceof AddAssociationEvent) {
            handleAssociationEvent(e);
        } else if (isBaseClassAttributeChangeEvent(e)) {
            handleBaseClassChangeEvent();
        } else {
            logUnneededEvent(e);
        }
    }

    private void handleAssociationEvent(final PropertyChangeEvent e) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    if (getterSetterManager.isFullBuildOnly(propertyName)) {
                        rebuildListModel();
                    } else {
                        handleIndividualAssociationEvent(e);
                    }
                } catch (InvalidElementException ex) {
                    LOG.log(Level.FINE, "propertyChange accessed a deleted element ", ex);
                }
            }
        });
    }

    private boolean isBaseClassAttributeChangeEvent(PropertyChangeEvent e) {
        return e.getPropertyName().equals("baseClass")
                && e.getPropertyName().equals(propertyName)
                && e instanceof AttributeChangeEvent;
    }

    private void handleBaseClassChangeEvent() {
        // TODO: We have some quirk that the a baseClass addition or
        // removal from a steroetype comes back as an AttributeChangeEvent
        // rather than an AssociationChangeEvent. This needs further
        // investigation to see if this can be made consistent.
        rebuildListModel();
    }

    private void logUnneededEvent(PropertyChangeEvent e) {
        LOG.log(Level.FINE,
                "We are listening for too much here. "
                        + "An event we don't need {0}",
                e);
    }

    private void rebuildListModel() {
        removeAllElements();
        build();
    }

    private void handleIndividualAssociationEvent(PropertyChangeEvent e) {
        if (e instanceof RemoveAssociationEvent) {
            removeElement(((RemoveAssociationEvent) e).getChangedValue());
        } else if (e instanceof AddAssociationEvent) {
            addElementIfValid(((AddAssociationEvent) e).getChangedValue());
        }
    }

    private void addElementIfValid(Object newElement) {
        if (isValidType(newElement) && !contains(newElement)) {
            addElementInCorrectPosition(newElement);
        }
    }

    private boolean isValidType(Object element) {
        for (Class<?> cls : metaTypes) {
            if (cls.isInstance(element)) {
                return true;
            }
        }
        return false;
    }

    private void addElementInCorrectPosition(Object newElement) {
        if (Model.getUmlHelper().isMovable(getMetaType())) {
            addElementInOrderedPosition(newElement);
        } else {
            addElement(newElement);
        }
    }

    private void addElementInOrderedPosition(Object newElement) {
        final Collection c =
                (Collection) getterSetterManager.getOptions(
                        umlElement,
                        propertyName,
                        metaTypes);
        final int index = CollectionUtil.indexOf(c, newElement);
        if (index < 0 || index > getSize() - 1) {
            LOG.log(Level.WARNING,
                    "Unable to add element at correct position "
                            + index + " added to end instead");
            addElement(newElement);
        } else {
            add(index, newElement);
        }
    }

//Refactoring end
            // rather than an AssociationChangeEvent. This needs further
            // investigation to see if this can be made consistent.
       	    removeAllElements();
            build();
        } else {
            LOG.log(Level.FINE,
                    "We are listening for too much here. "
                    + "An event we don't need {0}",
                    e);
        }
    }

    /**
     * Delete and rebuild the model list from scratch.
     */
    private void build() {
        try {
            LOG.log(Level.FINE,
                    "Getting options for {0} {1} {2}",
                    new Object[]{umlElement, propertyName, metaTypes});
            
            final Collection c = (Collection) getterSetterManager.getOptions(
                    umlElement,
                    propertyName,
                    metaTypes);
            for (Object o : c) {
                addElement(o);
            }
        } catch (InvalidElementException exception) {
            LOG.log(Level.FINE, "buildModelList threw exception for target "
                    + umlElement + ": "
                    + exception);
        }
    }

    public Object getUmlElement() {
        return umlElement;
    }

}
