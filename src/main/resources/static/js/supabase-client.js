// Supabase JavaScript Client for real-time features
(function() {
  // Supabase configuration
  const SUPABASE_URL = 'https://gbzwqsyoihqtpcionaze.supabase.co';
  const SUPABASE_ANON_KEY = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imdiendxc3lvaWhxdHBjaW9uYXplIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjQxNTIwODYsImV4cCI6MjA3OTcyODA4Nn0.zQgjlkrV7Q8i8cKrjdJm21qqbruFUPEs0-0lWMHTzlY';
  
  // Initialize Supabase client (if library is loaded)
  let supabaseClient = null;
  
  if (typeof supabase !== 'undefined') {
    supabaseClient = supabase.createClient(SUPABASE_URL, SUPABASE_ANON_KEY);
  }
  
  // Real-time subscription for campaigns
  function subscribeToCampaigns(callback) {
    if (!supabaseClient) {
      console.warn('Supabase client not initialized');
      return null;
    }
    
    return supabaseClient
      .channel('campaigns-changes')
      .on('postgres_changes', 
        { event: '*', schema: 'public', table: 'campaigns' }, 
        callback
      )
      .subscribe();
  }
  
  // Real-time subscription for donations
  function subscribeToDonations(campaignId, callback) {
    if (!supabaseClient) {
      console.warn('Supabase client not initialized');
      return null;
    }
    
    return supabaseClient
      .channel('donations-changes')
      .on('postgres_changes', 
        { 
          event: 'INSERT', 
          schema: 'public', 
          table: 'donations',
          filter: `campaign_id=eq.${campaignId}`
        }, 
        callback
      )
      .subscribe();
  }
  
  // Upload image to Supabase Storage
  async function uploadImage(file, bucket = 'campaign-images') {
    if (!supabaseClient) {
      throw new Error('Supabase client not initialized');
    }
    
    const fileExt = file.name.split('.').pop();
    const fileName = `${Math.random().toString(36).substring(2)}_${Date.now()}.${fileExt}`;
    
    const { data, error } = await supabaseClient.storage
      .from(bucket)
      .upload(fileName, file, {
        cacheControl: '3600',
        upsert: false
      });
    
    if (error) throw error;
    
    // Get public URL
    const { data: urlData } = supabaseClient.storage
      .from(bucket)
      .getPublicUrl(fileName);
    
    return urlData.publicUrl;
  }
  
  // Expose to window
  window.SupabaseClient = {
    client: supabaseClient,
    subscribeToCampaigns,
    subscribeToDonations,
    uploadImage,
    isInitialized: () => supabaseClient !== null
  };
})();
