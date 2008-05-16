/*
 * This file is part of DrFTPD, Distributed FTP Daemon.
 *
 * DrFTPD is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * DrFTPD is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DrFTPD; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.drftpd.vfs.index;

import java.util.Map;
import java.util.Set;

import org.drftpd.vfs.DirectoryHandle;
import org.drftpd.vfs.InodeHandle;
import org.drftpd.vfs.index.AdvancedSearchParams.InodeType;

/**
 * To create a new Indexing engine this interface must be implemented.
 * Further details of how it should work, check the rest of the API documentation.
 * @author fr0w
 * @version $Id$
 */
public interface IndexEngineInterface {
	/**
	 * This method should initialize the engine, making it ready to receive queries.<br>
	 * Below a list of what might be done here:
	 * <ul>
	 * <li>Estabilishing the connection to the database</li>
	 * <li>Read configuration parameters</li> 
	 * <li>Subscribe to some daemon event, like ReloadEvent.</li>
	 * <li>Etc...</li>
	 * </ul>
	 * @throws IndexException
	 */
	public void init() throws IndexException;

	/**
	 * Adds an inode to the index.<br>
	 * All indexes <b>must</b> contain at least this fields in the index:
	 * <ul>
	 * <li>path</li> 
	 * <li>owner - The user who owns the file</li>
	 * <li>group - The group of the user who owns the file</li>
	 * <li>type - File or Directory</li>
	 * <li>size - The size of the inode</li>
	 * <li>slaves - If the inode is a file, then the slaves are stored</li>
	 * </ul>
	 * If this pattern is not respected the behaviour is unpredictable.<br>
	 * You can add more fields if you happen to need them, but beware that you will
	 * also have to write your own code to make use of them.
	 * @param inode
	 * @throws IndexException
	 */
	public void addInode(InodeHandle inode) throws IndexException;
	
	/**
	 * Deletes an inode from the index.
	 * @param inode
	 * @throws IndexException
	 */
	public void deleteInode(InodeHandle inode) throws IndexException;
	
	/**
	 * Updates the data of an inode.
	 * @param inode
	 * @throws IndexException
	 */
	public void updateInode(InodeHandle inode) throws IndexException;
	
	/**
	 * Renames the inode in the index
	 * @param fromInode
	 * @param toInode
	 * @throws IndexException
	 */
	public void renameInode(InodeHandle fromInode, InodeHandle toInode) throws IndexException;
	
	/**
	 * Force the engine to save its current data.<br>
	 * Not all databases need this operation, if that's your case,
	 * do not enter any code while implenting this method.
	 * @throws IndexException
	 */
	public void commit() throws IndexException;
	
	/**
	 * Removes ALL the content from the Index and recurse through the site recreating the index.
	 */
	public void rebuildIndex() throws IndexException;
	
	/**
	 * This method should return a Map containing information about the index engine.<br>
	 * Any kind of information is allowed but there are twothat are mandatory:
	 * <ul>
	 * <li>Number of inodes (the key must be called "inodes")</li>
	 * <li>Storage backend (the key must be called "backend")</li>
	 * </ul>
	 * @return
	 */
	public Map<String, String> getStatus();
	
	public Set<String> findInode(DirectoryHandle startNode, String text, InodeType inodeType) throws IndexException;
	public Set<String> advancedFind(AdvancedSearchParams params) throws IndexException;
}
