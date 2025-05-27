import 'package:flutter/material.dart';

class DailyReportScreen extends StatelessWidget {
  const DailyReportScreen({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text(
          'RAPPORT JOURNALIER',
          style: TextStyle(
            color: Color(0xFF2D3748),
            fontSize: 24,
            fontWeight: FontWeight.bold,
            fontFamily: 'Inter',
          ),
        ),
        backgroundColor: Colors.white,
        elevation: 0,
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Header Section
            Card(
              elevation: 2,
              child: Padding(
                padding: const EdgeInsets.all(16.0),
                child: Row(
                  children: [
                    const Text(
                      'Site: ',
                      style: TextStyle(
                        fontSize: 16,
                        fontWeight: FontWeight.w500,
                        color: Color(0xFF4A5568),
                      ),
                    ),
                    const SizedBox(width: 8),
                    Expanded(
                      child: DropdownButtonFormField<String>(
                        decoration: const InputDecoration(
                          border: OutlineInputBorder(),
                          contentPadding: EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                        ),
                        hint: const Text('Select Site'),
                        items: ['Site 1', 'Site 2', 'Site 3']
                            .map((site) => DropdownMenuItem(
                                  value: site,
                                  child: Text(site),
                                ))
                            .toList(),
                        onChanged: (value) {},
                      ),
                    ),
                    const SizedBox(width: 16),
                    const Text(
                      'Date: ',
                      style: TextStyle(
                        fontSize: 16,
                        fontWeight: FontWeight.w500,
                        color: Color(0xFF4A5568),
                      ),
                    ),
                    const SizedBox(width: 8),
                    const Text(
                      '',
                      style: TextStyle(
                        fontSize: 16,
                        color: Color(0xFF4A5568),
                      ),
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 16),

            // Module 1
            _buildModule(
              title: 'Module 1',
              onAddStop: () {},
              rows: [
                ['', '', '', '']
              ],
            ),
            const SizedBox(height: 16),

            // Module 2
            _buildModule(
              title: 'Module 2',
              onAddStop: () {},
              rows: [
                ['', '', '', ''],
              ],
            ),
            const SizedBox(height: 16),

            // Totals Section
            Card(
              elevation: 2,
              child: Padding(
                padding: const EdgeInsets.all(16.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text(
                      'Totals',
                      style: TextStyle(
                        fontSize: 18,
                        fontWeight: FontWeight.w600,
                        color: Color(0xFF2D3748),
                      ),
                    ),
                    const SizedBox(height: 16),
                    Row(
                      children: [
                        Expanded(
                          child: _buildTotalItem('', ''),
                        ),
                        Expanded(
                          child: _buildTotalItem('', ''),
                        ),
                      ],
                    ),
                    const SizedBox(height: 8),
                    Row(
                      children: [
                        Expanded(
                          child: _buildTotalItem('', ''),
                        ),
                        Expanded(
                          child: _buildTotalItem('', ''),
                        ),
                      ],
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 24),

            // Action Buttons
            Row(
              mainAxisAlignment: MainAxisAlignment.end,
              children: [
                OutlinedButton(
                  onPressed: () {},
                  style: OutlinedButton.styleFrom(
                    padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
                    side: const BorderSide(color: Color(0xFFCBD5E0)),
                  ),
                  child: const Text(
                    'Enregistrer',
                    style: TextStyle(
                      color: Color(0xFF48BB78),
                      fontSize: 16,
                    ),
                  ),
                ),
                const SizedBox(width: 16),
                ElevatedButton(
                  onPressed: () {},
                  style: ElevatedButton.styleFrom(
                    backgroundColor: const Color(0xFF3B82F6),
                    padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
                  ),
                  child: const Text(
                    'Soumettre',
                    style: TextStyle(
                      color: Colors.white,
                      fontSize: 16,
                    ),
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildModule({
    required String title,
    required VoidCallback onAddStop,
    required List<List<String>> rows,
  }) {
    return Card(
      elevation: 2,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            padding: const EdgeInsets.all(16),
            decoration: const BoxDecoration(
              color: Color(0xFFF7FAFC),
              borderRadius: BorderRadius.vertical(top: Radius.circular(4)),
            ),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  title,
                  style: const TextStyle(
                    fontSize: 18,
                    fontWeight: FontWeight.w600,
                    color: Color(0xFF2D3748),
                  ),
                ),
                OutlinedButton(
                  onPressed: onAddStop,
                  style: OutlinedButton.styleFrom(
                    side: const BorderSide(color: Color(0xFFCBD5E0)),
                  ),
                  child: const Text('Ajouter Arrêt'),
                ),
              ],
            ),
          ),
          SingleChildScrollView(
            scrollDirection: Axis.horizontal,
            child: DataTable(
              headingTextStyle: const TextStyle(
                fontWeight: FontWeight.bold,
                fontSize: 14,
                color: Color(0xFF2D3748),
              ),
              dataTextStyle: const TextStyle(
                fontSize: 14,
                color: Color(0xFF4A5568),
              ),
              columns: const [
                DataColumn(label: Text('Durée')),
                DataColumn(label: Text('Nature')),
                DataColumn(label: Text('HM')),
                DataColumn(label: Text('HA')),
              ],
              rows: rows.map((row) {
                return DataRow(
                  cells: row.map((cell) => DataCell(Text(cell))).toList(),
                );
              }).toList(),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildTotalItem(String label, String value) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          label,
          style: const TextStyle(
            fontSize: 14,
            color: Color(0xFF718096),
          ),
        ),
        const SizedBox(height: 4),
        Text(
          value,
          style: const TextStyle(
            fontSize: 16,
            fontWeight: FontWeight.w500,
            color: Color(0xFF2D3748),
          ),
        ),
      ],
    );
  }
} 