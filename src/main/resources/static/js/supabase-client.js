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
      console.log('Initializing Supabase client...');
      const response = await fetch('/api/supabase/config');
      
      if (!response.ok) {
        console.warn(`Supabase config endpoint returned ${response.status}`);
        return;
      }
      
      const config = await response.json();
      
      if (config.error === "true" || config.error === true) {
        console.warn('⚠️ Supabase not configured:', config.message);
        console.info('💡 To enable Supabase features:');
        console.info('   1. Get credentials from https://supabase.com/dashboard');
        console.info('   2. Set environment variables:');
        console.info('      SUPABASE_URL=https://xxx.supabase.co');
        console.info('      SUPABASE_ANON_KEY=your-anon-key');
        return;
      }
      
      SUPABASE_URL = config.url;
      SUPABASE_ANON_KEY = config.anonKey;
      
      // Check if Supabase library is loaded
      if (typeof supabase === 'undefined') {
        console.error('❌ Supabase JS library not loaded!');
        console.info('💡 Add to HTML: <script src="https://cdn.jsdelivr.net/npm/@supabase/supabase-js@2"></script>');
        return;
      }
      
      supabaseClient = supabase.createClient(SUPABASE_URL, SUPABASE_ANON_KEY);
      console.log('✓ Supabase client initialized successfully');
      console.log('  URL:', SUPABASE_URL);
      console.log('  Storage Bucket:', config.storageBucket);
      
    } catch (error) {
      console.error('❌ Supabase initialization failed:', error.message);
      console.info('💡 Check that:');
      console.info('   1. Application is running');
      console.info('   2. /api/supabase/config endpoint is accessible');
      console.info('   3. Environment variables are set correctly');
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
      console.warn('Supabase client not initialized. Real-time features disabled.');
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
      console.warn('Supabase client not initialized. Real-time features disabled.');
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
      throw new Error('Supabase client not initialized. Cannot upload images.');
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
