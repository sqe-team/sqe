/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbheaven.sqe.codedefects.ui.actions;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.util.Collection;
import javafx.beans.value.ObservableValue;
import javax.swing.AbstractAction;
import org.nbheaven.sqe.codedefects.core.api.QualityProvider;
import org.nbheaven.sqe.codedefects.core.api.QualitySession;
import org.nbheaven.sqe.codedefects.core.util.SQECodedefectSupport;
import org.nbheaven.sqe.codedefects.ui.UIHandle;
import org.nbheaven.sqe.codedefects.ui.utils.UiUtils;
import org.nbheaven.sqe.core.utilities.SQEProjectSupport;
import org.netbeans.api.project.Project;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;

/**
 *
 * @author fvo
 */
public abstract class AbstractRunQualityProvidersAction extends AbstractAction {

    private final Lookup context;
    private final Lookup.Result<Node> lkpInfo;
    private QualitySession activeSession = null;
    private final QualityProvider provider;

    protected AbstractRunQualityProvidersAction(Lookup context, QualityProvider provider) {
        this.context = context;

        Lookup.Template<Node> tpl = new Lookup.Template<>(Node.class);
        lkpInfo = context.lookup(tpl);
        lkpInfo.addLookupListener(this::resultChanged);
        this.provider = provider;
    }

    private void resultChanged(LookupEvent ev) {
        Collection<? extends Node> nodes = lkpInfo.allInstances();
        if (nodes.size() == 1) {
            Project project = SQEProjectSupport.findProject(nodes.iterator().next());
            setActiveSession(SQECodedefectSupport.retrieveSession(project, provider));
        } else {
            setActiveSession(null);
        }
    }

    private void setActiveSession(QualitySession session) {
        if (activeSession != null) {
            activeSession.getEnabledProperty().removeListener(this::propertyChange);
        }
        activeSession = session;
        if (activeSession != null) {
            activeSession.getEnabledProperty().addListener(this::propertyChange);
        }
        updateEnableState();
    }

    private void propertyChange(ObservableValue<? extends Boolean> value, Boolean oldValue, Boolean newValue) {
        updateEnableState();
    }

    private void updateEnableState() {
        boolean enable = false;
        if (!EventQueue.isDispatchThread()) {
            EventQueue.invokeLater(this::updateEnableState);
        } else {
            setEnabled(null != activeSession && activeSession.isEnabled());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (activeSession.isEnabled()) {
            activeSession.computeResult();
            UIHandle uiHandle = UiUtils.getUIHandle(activeSession.getProvider());
            if (null != uiHandle) {
                uiHandle.open();
            }
        }
    }
}
