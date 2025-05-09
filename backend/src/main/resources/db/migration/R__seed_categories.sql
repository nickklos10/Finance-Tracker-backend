INSERT INTO categories (name, description) VALUES
                                               -- Essentials
                                               ('Housing',        'Rent or mortgage payments'),
                                               ('Utilities',      'Electricity, gas, water, trash, internet'),
                                               ('Groceries',      'Food & household supplies'),
                                               ('Transportation', 'Fuel, public transit, ride‑shares'),
                                               ('Insurance',      'Health, auto, home premiums'),
                                               ('Medical',        'Doctor visits, prescriptions, dental, vision'),
                                               ('Debt',           'Loan or credit‑card payments'),
                                               -- Lifestyle & variable
                                               ('Dining Out',     'Restaurants, cafés, take‑away'),
                                               ('Entertainment',  'Movies, concerts, events, hobbies'),
                                               ('Subscriptions',  'Streaming and digital services'),
                                               ('Personal Care',  'Haircuts, cosmetics, gym membership'),
                                               ('Clothing',       'Apparel, shoes, accessories'),
                                               -- Goals & irregular
                                               ('Savings',        'Emergency fund, investments'),
                                               ('Gifts',          'Gifts & charitable donations'),
                                               ('Travel',         'Flights, hotels, vacation expenses')
    ON CONFLICT (name) DO NOTHING;     -- keeps script idempotent
