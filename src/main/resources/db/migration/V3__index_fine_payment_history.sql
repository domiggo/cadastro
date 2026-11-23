CREATE INDEX idx_tb_pagamentos_emprestimo_data_id
    ON tb_pagamentos (emprestimo_id, data_pagamento, id);
