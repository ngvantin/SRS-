import Link from "next/link"
import { Button } from "@/components/ui/button"

export default function NotFound() {
  return (
    <div className="container mx-auto py-12 text-center">
      <h2 className="text-2xl font-bold mb-4">Page Not Found</h2>
      <p className="mb-6">Could not find the requested role creation page.</p>
      <Button asChild>
        <Link href="/roles">Return to Roles</Link>
      </Button>
    </div>
  )
}

