-- Fix payments table constraints and add missing columns

-- Drop existing foreign key if exists
DO $$ 
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'payments_donation_id_fkey'
    ) THEN
        ALTER TABLE payments DROP CONSTRAINT payments_donation_id_fkey;
    END IF;
END $$;

-- Add payment_method to donations if not exists
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'donations' AND column_name = 'payment_method'
    ) THEN
        ALTER TABLE donations ADD COLUMN payment_method VARCHAR(20) DEFAULT 'direct';
    END IF;
END $$;

-- Recreate foreign key with ON DELETE SET NULL
ALTER TABLE payments 
ADD CONSTRAINT fk_payments_donation 
FOREIGN KEY (donation_id) 
REFERENCES donations(id) 
ON DELETE SET NULL;

-- Create index if not exists
CREATE INDEX IF NOT EXISTS idx_payments_donation_id ON payments(donation_id);
CREATE INDEX IF NOT EXISTS idx_payments_vnpay_txn_ref ON payments(vnpay_txn_ref);
CREATE INDEX IF NOT EXISTS idx_payments_status ON payments(payment_status);

-- Update trigger if not exists
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = CURRENT_TIMESTAMP;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS update_payments_updated_at ON payments;
CREATE TRIGGER update_payments_updated_at 
  BEFORE UPDATE ON payments
  FOR EACH ROW 
  EXECUTE FUNCTION update_updated_at_column();
