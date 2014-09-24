/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2014 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.crm.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.axelor.app.AppSettings;
import com.axelor.apps.base.service.MapService;
import com.axelor.apps.crm.db.Opportunity;
import com.axelor.apps.crm.service.OpportunityService;
import com.axelor.auth.AuthUtils;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class OpportunityController {
	
	@Inject
	Provider<OpportunityService> OpportunityProvider;
	
	
	@Inject
	Provider<MapService> mapProvider;
	
	public void saveOpportunitySalesStage(ActionRequest request, ActionResponse response) throws AxelorException {
		Opportunity opportunity = request.getContext().asType(Opportunity.class);
		Opportunity persistOpportunity = OpportunityProvider.get().find(opportunity.getId());
		persistOpportunity.setSalesStageSelect(opportunity.getSalesStageSelect());
		OpportunityProvider.get().saveOpportunity(persistOpportunity);
	}
	
	public void assignToMe(ActionRequest request, ActionResponse response)  {
		if(request.getContext().get("id") != null){
			Opportunity opportunity = OpportunityProvider.get().find((Long)request.getContext().get("id"));
			opportunity.setUser(AuthUtils.getUser());
			OpportunityProvider.get().saveOpportunity(opportunity);
		}
		else if(!((List)request.getContext().get("_ids")).isEmpty()){
			for(Opportunity opportunity : OpportunityProvider.get().all().filter("id in ?1",request.getContext().get("_ids")).fetch()){
				opportunity.setUser(AuthUtils.getUser());
				OpportunityProvider.get().saveOpportunity(opportunity);
			}
		}
		response.setReload(true);
	}
	
	public void showOpportunitiesOnMap(ActionRequest request, ActionResponse response) throws IOException {
		
		String appHome = AppSettings.get().get("application.home");
		if (Strings.isNullOrEmpty(appHome)) {
			response.setFlash("Can not open map, Please Configure Application Home First.");
			return;
		}
		if (!mapProvider.get().isInternetAvailable()) {
			response.setFlash("Can not open map, Please Check your Internet connection.");
			return;			
		}		
		String mapUrl = new String(appHome + "/map/gmap-objs.html?apphome=" + appHome + "&object=opportunity");
		Map<String, Object> mapView = new HashMap<String, Object>();
		mapView.put("title", "Opportunities");
		mapView.put("resource", mapUrl);
		mapView.put("viewType", "html");		
		response.setView(mapView);
	}	
}
