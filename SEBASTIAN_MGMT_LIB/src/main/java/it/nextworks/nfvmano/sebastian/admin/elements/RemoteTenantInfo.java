package it.nextworks.nfvmano.sebastian.admin.elements;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;

@Entity
public class RemoteTenantInfo {
    @Id
    @GeneratedValue
    @JsonIgnore
    private Long id;

    private String remoteTenantName;
    private String remoteTenantPwd;
    private String host;

    public RemoteTenantInfo(){
        //FOR JPA
    }

    public RemoteTenantInfo(String remoteTenantName, String remoteTenantPwd, String host){
        this.remoteTenantName =remoteTenantName;
        this.remoteTenantPwd=remoteTenantPwd;
        this.host=host;
    }

    public String getRemoteTenantName() {
        return remoteTenantName;
    }

    public void setRemoteTenantName(String remoteTenantName) {
        this.remoteTenantName = remoteTenantName;
    }

    public String getHost(){
        return this.host;
    }

    public Long getId() {
        return id;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getRemoteTenantPwd() {
        return remoteTenantPwd;
    }
}
