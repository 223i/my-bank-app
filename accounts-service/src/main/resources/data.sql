-- Очистка перед вставкой (если нужно для тестов)
TRUNCATE TABLE accounts RESTART IDENTITY CASCADE;

INSERT INTO accounts (login, name, birthday, sum) VALUES
-- Обычный пользователь с положительным балансом
('jdoe', 'John Doe', '1990-05-15 00:00:00+00', 150000),

-- Пользователь с нулевым балансом (граница констрейнта)
('rich_admin', 'Administrator', '1985-12-31 23:59:59+00', 0),

-- Молодой пользователь (проверка TIMESTAMP)
('alice_99', 'Alice Smith', '2005-01-01 10:30:00+03', 5000000),

-- Пользователь с очень длинным именем
('test_user', 'Konstantin Konstantinopolsky-Voznesensky', NULL, 100);