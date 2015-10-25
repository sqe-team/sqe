/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nbheaven.sqe.tools.checkstyle.codedefects.core.internal;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Florian Vogler
 */
public final class AuditEventSupport {

    private AuditEventSupport() {
    }

    public static String getRelativeProjectFilePath(Project project, FileObject fileObject) {
        return getRelativeProjectFilePath(project, fileObject.getPath());
    }

    public static String getRelativeProjectFilePath(Project project, AuditEvent auditEvent) {
        return getRelativeProjectFilePath(project, auditEvent.getFileName());
    }

    private static String getRelativeProjectFilePath(Project project, String fileName) {
        String path = FileUtil.toFile(project.getProjectDirectory()).getAbsolutePath();
        fileName = fileName.replaceAll("\\\\", "/");
        if (fileName.startsWith(path)) {
            fileName = fileName.substring(path.length() + 1);
        }
        return fileName;
    }
}
