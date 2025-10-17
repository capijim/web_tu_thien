package org.example.webtuthien.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LegacyRouteController {

    @GetMapping({"/index.html"})
    public String legacyIndex() { return "index"; }

    @GetMapping({"/login.html"})
    public String legacyLogin() { return "login"; }

    @GetMapping({"/register.html"})
    public String legacyRegister() { return "register"; }

    @GetMapping({"/campaigns.html"})
    public String legacyCampaigns() { return "campaigns"; }

    @GetMapping({"/campaign_detail.html"})
    public String legacyCampaignDetail() { return "campaign_detail"; }

    @GetMapping({"/change_password.html"})
    public String legacyChangePassword() { return "change_password"; }

	// Support fetching headbar via JS
	@GetMapping({"/components/headbar.html"})
	public String headbarFragment() { return "components/headbar"; }
}


