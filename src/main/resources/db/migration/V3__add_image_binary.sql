-- Add binary image storage to campaigns table

ALTER TABLE campaigns ADD COLUMN IF NOT EXISTS image_data BYTEA;
ALTER TABLE campaigns ADD COLUMN IF NOT EXISTS image_mime_type VARCHAR(50);
ALTER TABLE campaigns ADD COLUMN IF NOT EXISTS image_size BIGINT;

-- Add comment
COMMENT ON COLUMN campaigns.image_data IS 'Binary image data (max 5MB recommended)';
COMMENT ON COLUMN campaigns.image_mime_type IS 'MIME type: image/jpeg, image/png, image/webp';
COMMENT ON COLUMN campaigns.image_size IS 'Image size in bytes';
