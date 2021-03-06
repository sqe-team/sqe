///* Copyright 2005,2006 Sven Reimers, Florian Vogler
// *
// * This file is part of the Software Quality Environment Project.
// *
// * The Software Quality Environment Project is free software:
// * you can redistribute it and/or modify it under the terms of the
// * GNU General Public License as published by the Free Software Foundation,
// * either version 2 of the License, or (at your option) any later version.
// *
// * The Software Quality Environment Project is distributed in the hope that
// * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
// * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
// * See the GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
// */
//package org.nbheaven.sqe.tools.pmd.codedefects.core.annotations;
//
//import javax.swing.Action;
//import javax.swing.ImageIcon;
//import org.nbheaven.sqe.tools.pmd.codedefects.core.PMDQualityProvider;
//import org.nbheaven.sqe.codedefects.core.api.QualityProvider;
//import org.nbheaven.sqe.codedefects.ui.actions.AbstractShowAnnotationsAction;
//import org.openide.util.ImageUtilities;
//import org.openide.util.Lookup;
//
///**
// *
// * @author Sven Reimers
// */
//public class PMDAnnotationToggleAction extends AbstractShowAnnotationsAction {
//    
//    public PMDAnnotationToggleAction() {
//        this(Lookup.EMPTY);
//    }
//
//    private PMDAnnotationToggleAction(Lookup lookup) {
//        super(lookup);
//        putValue(Action.SMALL_ICON, new ImageIcon(ImageUtilities.loadImage( "org/nbheaven/sqe/tools/pmd/codedefects/core/resources/pmd.png")));
//    }
//    
//    @Override
//    public Action createContextAwareInstance(Lookup lookup) {
//        return new PMDAnnotationToggleAction(lookup);
//    }
//    
//    @Override
//    protected QualityProvider getQualityProvider() {
//        return PMDQualityProvider.getDefault();
//    }
//    
//}
