'use client';

import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { useWorkflowContext } from '@/components/providers/WorkflowProvider';
import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter';
import { darcula } from 'react-syntax-highlighter/dist/esm/styles/prism';

export function ResponsePanel() {
  const { responseText } = useWorkflowContext();

  return (
    <Card className="flex-1">
      <CardHeader>
        <CardTitle>API Response</CardTitle>
      </CardHeader>
      <CardContent>
        <div 
          className="overflow-auto max-h-[400px] border-white/20 border"
          role="region"
          aria-live="polite"
          aria-atomic="true"
          aria-labelledby="api-response-heading"
        >
          <SyntaxHighlighter
            language="json"
            style={darcula}
            customStyle={{ 
              margin: 0, 
              background: '#0b0b0b', 
              fontSize: 12,
            }}
            wrapLongLines={true}
            showLineNumbers={false}
          >
            {responseText || '"Response will appear here"'}
          </SyntaxHighlighter>
        </div>
      </CardContent>
    </Card>
  );
}