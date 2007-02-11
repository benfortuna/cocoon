/*
 * Copyright 2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.deployer.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import org.apache.cocoon.deployer.DeploymentException;
import org.apache.cocoon.deployer.filemanager.FileManager;
import org.apache.cocoon.deployer.filemanager.NontransactionalFileManager;
import org.apache.cocoon.deployer.filemanager.TransactionalFileManager;

/**
 * Utitily class to handle ZIP archives.
 */
public class FileUtils {

	/**
	 * Delete a directory recursivly
	 * @param directory
	 * @return true if deletation went okay
	 */
    public static boolean deleteDirRecursivly(File directory) {
        if (directory.isDirectory()) {
            String[] children = directory.list();
            for (int i=0; i < children.length; i++) {
                boolean success = deleteDirRecursivly(new File(directory, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return directory.delete();
    }	

    /**
     * Factory method, that creates a FileManager, either a transactional or a
     * non-transaction one.
     */
	public static FileManager createFileManager(URI basedir, boolean transactional) {
		if(transactional) {
			return new TransactionalFileManager(basedir);
		}
		return new NontransactionalFileManager(basedir);
	}

	/**
	 * Create the directories of a non-exisiting file.
	 */
	public static File createDirectory(File file) throws IOException {
		if(file.isDirectory() || file.exists()) {
			return file;
		}
		String absolutePath = file.getCanonicalPath();
		String absolutePathDir = absolutePath.substring(0, absolutePath.lastIndexOf(File.separator));
		File absolutePathDirFile = new File(absolutePathDir);
		if(absolutePathDirFile.exists()) {
			return file;
		}
		if(!new File(absolutePathDir).mkdirs()) {
			throw new DeploymentException("Can't create directory '" + absolutePathDir + "'");
		}
		return file;
	}

	/**
	 * Copies an inputstream to an outputstream.
	 */
	public static void copy(InputStream fis, OutputStream out) throws IOException {
		try {
			byte[] buf = new byte[1024];
			int len;
			while ((len = fis.read(buf)) > 0) {
			    out.write(buf, 0, len);
			}
		} finally {
			fis.close();
			out.close();
		}
	}
	
}