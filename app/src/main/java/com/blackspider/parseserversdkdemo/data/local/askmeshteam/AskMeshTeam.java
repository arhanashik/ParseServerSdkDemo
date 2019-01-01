package com.blackspider.parseserversdkdemo.data.local.askmeshteam;
/*
 *  ****************************************************************************
 *  * Created by : Arhan Ashik on 7/2/2018 at 1:01 PM.
 *  * Email : ashik.pstu.cse@gmail.com
 *  *
 *  * Last edited by : Arhan Ashik on 7/2/2018.
 *  *
 *  * Last Reviewed by : <Reviewer Name> on <mm/dd/yy>
 *  ****************************************************************************
 */

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("AskMeshTeam")
public class AskMeshTeam extends ParseObject {
    private String TEAM_NAME = "team_name";

    public String getTeamName(){
        return getString(TEAM_NAME);
    }

    public void setTeamName(String teamName){
        put(TEAM_NAME, teamName);
    }
}
