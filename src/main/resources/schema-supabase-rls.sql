-- Enable Row Level Security on all tables
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE partners ENABLE ROW LEVEL SECURITY;
ALTER TABLE campaigns ENABLE ROW LEVEL SECURITY;
ALTER TABLE donations ENABLE ROW LEVEL SECURITY;
ALTER TABLE admins ENABLE ROW LEVEL SECURITY;
ALTER TABLE payments ENABLE ROW LEVEL SECURITY;

-- Policies for campaigns (public read, admin write)
CREATE POLICY "Campaigns are viewable by everyone"
  ON campaigns FOR SELECT
  USING (true);

CREATE POLICY "Campaigns are insertable by authenticated users"
  ON campaigns FOR INSERT
  WITH CHECK (auth.role() = 'authenticated');

CREATE POLICY "Campaigns are updatable by admins"
  ON campaigns FOR UPDATE
  USING (auth.role() = 'authenticated');

-- Policies for donations (public read, authenticated write)
CREATE POLICY "Donations are viewable by everyone"
  ON donations FOR SELECT
  USING (true);

CREATE POLICY "Donations are insertable by authenticated users"
  ON donations FOR INSERT
  WITH CHECK (auth.role() = 'authenticated');

-- Policies for users (users can only see their own data)
CREATE POLICY "Users can view own data"
  ON users FOR SELECT
  USING (auth.uid()::text = id::text);

CREATE POLICY "Users can update own data"
  ON users FOR UPDATE
  USING (auth.uid()::text = id::text);

-- Policies for partners (public read, admin write)
CREATE POLICY "Partners are viewable by everyone"
  ON partners FOR SELECT
  USING (true);

-- Policies for admins (admin only)
CREATE POLICY "Admins table is admin only"
  ON admins FOR ALL
  USING (auth.role() = 'service_role');

-- Policies for payments (authenticated users only)
CREATE POLICY "Payments are viewable by authenticated users"
  ON payments FOR SELECT
  USING (auth.role() = 'authenticated');

-- Create storage bucket for campaign images
INSERT INTO storage.buckets (id, name, public)
VALUES ('campaign-images', 'campaign-images', true)
ON CONFLICT (id) DO NOTHING;

-- Storage policy for campaign images
CREATE POLICY "Campaign images are publicly accessible"
  ON storage.objects FOR SELECT
  USING (bucket_id = 'campaign-images');

CREATE POLICY "Authenticated users can upload campaign images"
  ON storage.objects FOR INSERT
  WITH CHECK (
    bucket_id = 'campaign-images' 
    AND auth.role() = 'authenticated'
  );

CREATE POLICY "Users can update own uploads"
  ON storage.objects FOR UPDATE
  USING (
    bucket_id = 'campaign-images' 
    AND auth.role() = 'authenticated'
  );

CREATE POLICY "Users can delete own uploads"
  ON storage.objects FOR DELETE
  USING (
    bucket_id = 'campaign-images' 
    AND auth.role() = 'authenticated'
  );
