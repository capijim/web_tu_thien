// Supabase JavaScript Client for real-time features
(function() {
  // Supabase configuration - will be loaded from backend
  let SUPABASE_URL = '';
  let SUPABASE_ANON_KEY = '';
  
  // Initialize Supabase client (if library is loaded)
  let supabaseClient = null;
  
  // Load configuration from backend
  async function initializeSupabase() {
    try {
      const response = await fetch('/api/supabase/config');
      const config = await response.json();
      
      SUPABASE_URL = config.url;
      SUPABASE_ANON_KEY = config.anonKey;
      
      if (typeof supabase !== 'undefined') {
        supabaseClient = supabase.createClient(SUPABASE_URL, SUPABASE_ANON_KEY);
        console.log('Supabase client initialized successfully');
      } else {
        console.warn('Supabase library not loaded');
      }
    } catch (error) {
      console.error('Failed to load Supabase config:', error);
    }
  }
  
  // Initialize on page load
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initializeSupabase);
  } else {
    initializeSupabase();
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
    isInitialized: () => supabaseClient !== null,
    initialize: initializeSupabase
  };
})();
