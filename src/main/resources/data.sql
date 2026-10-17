INSERT INTO programs (title, description, level, duration, category, active)
SELECT 'Digital Confidence and Office Productivity',
       'Practice computer basics, email, online documents, spreadsheets, and safe internet use for study and work.',
       'Beginner', '4 weeks', 'Digital Skills', TRUE
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM programs
    WHERE title = 'Digital Confidence and Office Productivity'
      AND category = 'Digital Skills'
);

INSERT INTO programs (title, description, level, duration, category, active)
SELECT 'Web Development Foundations',
       'Learn HTML, CSS, and introductory JavaScript by building and publishing a simple portfolio page.',
       'Beginner', '8 weeks', 'Technology', TRUE
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM programs
    WHERE title = 'Web Development Foundations'
      AND category = 'Technology'
);

INSERT INTO programs (title, description, level, duration, category, active)
SELECT 'Small Business Planning for Entrepreneurs',
       'Develop a practical business idea, identify customers, estimate costs, set prices, and create a starter business plan.',
       'Beginner', '6 weeks', 'Business', TRUE
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM programs
    WHERE title = 'Small Business Planning for Entrepreneurs'
      AND category = 'Business'
);

INSERT INTO programs (title, description, level, duration, category, active)
SELECT 'Personal Finance and Digital Banking',
       'Build budgeting and saving habits, understand everyday financial services, and learn safer use of digital banking.',
       'Beginner', '4 weeks', 'Finance', TRUE
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM programs
    WHERE title = 'Personal Finance and Digital Banking'
      AND category = 'Finance'
);

INSERT INTO programs (title, description, level, duration, category, active)
SELECT 'Workplace Communication and Interview Skills',
       'Practice professional communication, prepare a resume, and build confidence for job applications and interviews.',
       'Beginner', '5 weeks', 'Communication', TRUE
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM programs
    WHERE title = 'Workplace Communication and Interview Skills'
      AND category = 'Communication'
);
