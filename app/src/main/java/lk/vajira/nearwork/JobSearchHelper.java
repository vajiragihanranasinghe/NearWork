package lk.vajira.nearwork;

import java.util.ArrayList;
import java.util.List;

public class JobSearchHelper {
    
    // Mock job data for now (will connect to real API later)
    public static List<String> getNearbyJobs(String query, String location) {
        List<String> jobs = new ArrayList<>();
        
        jobs.add("💼 Software Engineer - Tech Corp\n📏 2.3km away\n💰 $80k-120k");
        jobs.add("💼 Data Analyst - DataFlow\n📏 1.5km away\n💰 $65k-90k");
        jobs.add("💼 UX Designer - Creative Inc\n📏 3.1km away\n💰 $70k-100k");
        jobs.add("💼 Project Manager - MegaCorp\n📏 4.2km away\n💰 $90k-130k");
        jobs.add("💼 Frontend Developer - WebStudio\n📏 0.8km away\n💰 $75k-110k");
        jobs.add("💼 Sales Representative - SalesForce\n📏 5.0km away\n💰 $50k-80k");
        jobs.add("💼 Marketing Specialist - BrandAgency\n📏 2.8km away\n💰 $55k-75k");
        jobs.add("💼 Customer Support - HelpDesk\n📏 1.2km away\n💰 $40k-55k");
        
        return jobs;
    }
}
