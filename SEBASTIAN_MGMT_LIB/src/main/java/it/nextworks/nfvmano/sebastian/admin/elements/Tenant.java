/*
* Copyright 2018 Nextworks s.r.l.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package it.nextworks.nfvmano.sebastian.admin.elements;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import it.nextworks.nfvmano.libs.ifa.common.enums.OperationalState;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.AlreadyExistingEntityException;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.MalformattedElementException;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.NotExistingEntityException;
import org.hibernate.annotations.*;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Tenant {

	@Id
    @GeneratedValue
    @JsonIgnore
    private Long id;
	
	@ManyToOne
	@JsonIgnore
	private TenantGroup group;
	
	private String username;
	private String password;
	
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	@OneToMany(mappedBy = "tenant", cascade=CascadeType.ALL, fetch=FetchType.EAGER, orphanRemoval=true)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@LazyCollection(LazyCollectionOption.FALSE)
	private List<Sla> sla = new ArrayList<>();
	
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	@ElementCollection(fetch=FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	@Cascade(org.hibernate.annotations.CascadeType.ALL)
	private List<String> vsdId = new ArrayList<>();
	
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	@ElementCollection(fetch=FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	@Cascade(org.hibernate.annotations.CascadeType.ALL)
	private List<String> vsiId = new ArrayList<>();

	@ElementCollection(fetch=FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	@Cascade(org.hibernate.annotations.CascadeType.ALL)
	private List<RemoteTenantInfo> remoteTenantInfos = new ArrayList<>();

	//This is to be changed to better manage MEC vs Cloud resources
	@Embedded
	private VirtualResourceUsage allocatedResources;
	
	public Tenant() { }
	
	public Tenant(TenantGroup group,
			String username, 
			String password) {
		this.group = group;
		this.username = username;
		this.password = password;
		this.allocatedResources = new VirtualResourceUsage(0, 0, 0);
	}

	public Tenant(TenantGroup group,
				  String username,
				  String password,
				  List<RemoteTenantInfo> remoteTenantInfos) {
		this.group = group;
		this.username = username;
		this.password = password;
		this.allocatedResources = new VirtualResourceUsage(0, 0, 0);
		this.remoteTenantInfos = remoteTenantInfos;
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @return the group
	 */
	public TenantGroup getGroup() {
		return group;
	}
	
	

	/**
	 * @param group the group to set
	 */
	public void setGroup(TenantGroup group) {
		this.group = group;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @return the sla
	 */
	public List<Sla> getSla() {
		return sla;
	}

	/**
	 * @return the vsdId
	 */
	public List<String> getVsdId() {
		return vsdId;
	}

	/**
	 * @return the vsiId
	 */
	public List<String> getVsiId() {
		return vsiId;
	}
	
	public void addVsd(String vsdId) {
		if (!(this.vsdId.contains(vsdId)))
			this.vsdId.add(vsdId);
	}
	
	public void removeVsd(String vsdId) {
		if (this.vsdId.contains(vsdId))
			this.vsdId.remove(vsdId);
	}
	
	public void addVsi(String vsiId) {
		if (!(this.vsiId.contains(vsiId)))
			this.vsiId.add(vsiId);
	}
	
	public void removeVsi(String vsiId) {
		if (this.vsiId.contains(vsiId))
			this.vsiId.remove(vsiId);
	}
	
	/**
	 * @return the allocatedResources
	 */
	public VirtualResourceUsage getAllocatedResources() {
		return allocatedResources;
	}

	public void addUsedResources(VirtualResourceUsage vru) {
		allocatedResources.addResources(vru);
	}

	public void removeUsedResources(VirtualResourceUsage vru) {
		allocatedResources.removeResources(vru);
	}
	
	@JsonIgnore
	public Sla getActiveSla() throws NotExistingEntityException {
		for (Sla s : sla) {
			if (s.getSlaStatus() == OperationalState.ENABLED) return s;
		}
		throw new NotExistingEntityException("Not found enabled SLA for tenant " + username);
	}

	public void isValid() throws MalformattedElementException {
		if (username == null) throw new MalformattedElementException("Tenant without username");
		if (password == null) throw new MalformattedElementException("Tenant without password");
	}

	public List<RemoteTenantInfo> getRemoteTenantInfos() {
		return remoteTenantInfos;
	}

	public void addRemoteTenantInfo(RemoteTenantInfo newRemoteTenantInfo) throws AlreadyExistingEntityException {
		for(RemoteTenantInfo remoteTenantInfo: remoteTenantInfos){
			if(remoteTenantInfo.getRemoteTenantName().equals(newRemoteTenantInfo.getRemoteTenantName())
					&& remoteTenantInfo.getHost().equals(newRemoteTenantInfo.getHost())){
				throw new AlreadyExistingEntityException("Association between tenant "+this.username+" and remoteTenantInfo with ID "+remoteTenantInfo.getId()+" already available");
			}
		}
		this.remoteTenantInfos.add(newRemoteTenantInfo);
	}

	public void removeRemoteTenantInfo(RemoteTenantInfo newRemoteTenantInfo) {
		int index=-1;
		Boolean found = false;
		for(int i=0; i<remoteTenantInfos.size(); i++){
			if(remoteTenantInfos.get(i).getRemoteTenantName().equals(newRemoteTenantInfo.getRemoteTenantName()) &&
					(remoteTenantInfos.get(i).getHost().equals(newRemoteTenantInfo.getHost()))){
					index = i;
					found=true;
					break;
				}
			}
		if(found==true)
			remoteTenantInfos.remove(index);
	}
}
